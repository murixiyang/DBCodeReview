package ic.ac.uk.db_pcr_backend.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.eclipse.jgit.api.CherryPickResult;
import org.eclipse.jgit.api.CherryPickResult.CherryPickStatus;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.ChangeIdUtil;

import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.api.projects.ProjectInput;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;
import com.urswolfer.gerrit.client.rest.http.HttpStatusException;

import ic.ac.uk.db_pcr_backend.entity.SubmissionTrackerEntity;
import ic.ac.uk.db_pcr_backend.repository.SubmissionTrackerRepository;

@Service
public class GerritService {
    private final GitLabService gitLabSvc;

    private final String gerritHttpUrl;
    private final String gerritAuthUrl;
    private final String gerritUsername;
    private final String gerritHttpPassword;
    private final String gerritBranch;

    private final GerritAuthData.Basic gerritAuthData;
    private final GerritRestApiFactory gerritApiFactory;

    private final SubmissionTrackerRepository submissionTrackerRepo;

    public GerritService(
            GitLabService gitLabService,
            @Value("${gerrit.url}") String gerritHttpUrl,
            @Value("${gerrit.auth.url}") String gerritAuthUrl,
            @Value("${gerrit.username}") String gerritUsername,
            @Value("${gerrit.password}") String gerritHttpPassword,
            @Value("${gerrit.branch:master}") String gerritBranch,
            SubmissionTrackerRepository submissionTrackerRepo) {

        this.gitLabSvc = gitLabService;
        this.gerritHttpUrl = gerritHttpUrl;
        this.gerritAuthUrl = gerritAuthUrl;
        this.gerritUsername = gerritUsername;
        this.gerritHttpPassword = gerritHttpPassword;
        this.gerritBranch = gerritBranch;

        this.gerritAuthData = new GerritAuthData.Basic(
                gerritHttpUrl, gerritUsername, gerritHttpPassword);
        this.gerritApiFactory = new GerritRestApiFactory();
        this.submissionTrackerRepo = submissionTrackerRepo;
    }

    public String submitForReview(String projectId, String sha, String gitlabToken, String username) throws Exception {
        String cloneUrl = gitLabSvc.getProjectCloneUrl(projectId, gitlabToken);
        String pathWithNamespace = gitLabSvc.getProjectPathWithNamespace(projectId, gitlabToken);

        GerritApi gerritApi = gerritApiFactory.create(gerritAuthData);
        CredentialsProvider gitlabCreds = new UsernamePasswordCredentialsProvider("oauth2", gitlabToken);
        CredentialsProvider gerritCredits = new UsernamePasswordCredentialsProvider(
                gerritUsername, gerritHttpPassword);

        // --- 1) Ensure the Gerrit project exists (create if missing)
        ensureGerritProjectExists(gerritApi, pathWithNamespace);

        // Load last submitted SHA from DB
        SubmissionTrackerEntity tracker = submissionTrackerRepo.findByUsernameAndProjectId(username, projectId)
                .orElseGet(() -> new SubmissionTrackerEntity(username, projectId, null));
        String baseSha = tracker.getLastSubmittedSha();

        Path tempDir = Files.createTempDirectory("review-");

        // Clone Gerrit
        Git git = Git.cloneRepository()
                .setURI(gerritAuthUrl + "/" + pathWithNamespace + ".git")
                .setDirectory(tempDir.toFile())
                .setBranch("refs/heads/" + gerritBranch)
                .setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider(gerritUsername, gerritHttpPassword))
                .call();

        git.fetch()
                .setRemote("origin")
                .setRefSpecs(new RefSpec("+refs/heads/*:refs/remotes/origin/*"), // your branch
                        new RefSpec("+refs/changes/*:refs/remotes/gerrit-changes/*"))
                .setCredentialsProvider(gerritCredits)
                .call();

        // 2) Fetch GitLab into that same repo
        git.remoteAdd()
                .setName("gitlab")
                .setUri(new URIish(cloneUrl))
                .call();

        git.fetch()
                .setRemote("gitlab")
                .setCredentialsProvider(gitlabCreds)
                .setRefSpecs(new RefSpec("+refs/heads/*:refs/remotes/gitlab/*"))
                .call();

        // Parse the base & target commits
        RevCommit base = (baseSha != null) ? parseCommit(git, baseSha) : null;
        RevCommit target = parseCommit(git, sha);

        // 3) Create and check out a new branch at base (or start from default if no
        // base)
        if (base != null) {
            git.checkout()
                    .setCreateBranch(true)
                    .setName("review/" + sha)
                    .setStartPoint(base)
                    .call();
        } else {
            // first review off Gerrit’s branch head
            git.checkout()
                    .setCreateBranch(true)
                    .setName("review/" + sha)
                    .setStartPoint("refs/heads/" + gerritBranch)
                    .call();
        }

        // Compute changeId for target
        String changeId = computeChangeId(target);

        // --- 5) Add Change-Id to this commit
        squashMergeCommit(git, target);
        addChangeId(git, changeId, target.getFullMessage());

        // --- 6) Push into Gerrit’s refs/for/<branch>
        pushForReview(git, pathWithNamespace);

        PushResult pr = git.push()
                .setRemote("origin")
                .setRefSpecs(new RefSpec("HEAD:refs/for/" + gerritBranch))
                .setCredentialsProvider(gerritCredits)
                .call()
                .iterator().next(); // assume single result

        String gerritCommitSha = pr.getRemoteUpdates().iterator().next().getNewObjectId().getName();

        // --- 7) Record the SHA as the new last submitted
        tracker.setLastSubmittedSha(gerritCommitSha);
        submissionTrackerRepo.save(tracker);

        System.out.println("DBLOG: Pushed commit to Gerrit: " + changeId);
        return changeId;
    }

    private void ensureGerritProjectExists(GerritApi api, String path) throws RestApiException {
        try {
            api.projects().name(path).get();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof HttpStatusException hse && hse.getStatusCode() == 404) {

                createEmptyGerritProject(path, api);
            } else {
                System.out.println("ERROR: Failed to check Gerrit project: " + e.getMessage());
                throw e;
            }
        }
    }

    private Optional<ChangeInfo> findExistingChange(GerritApi api,
            String changeId)
            throws RestApiException {
        // Gerrit supports direct lookup of a change by its Change-Id:
        try {
            ChangeInfo info = api.changes().id(changeId).get();
            return Optional.of(info);
        } catch (RestApiException e) {
            // If not found, Gerrit will throw a 404 — meaning no such change yet
            if (e instanceof HttpStatusException hse && hse.getStatusCode() == 404) {
                return Optional.empty();
            }
            throw e;
        }
    }

    private void createEmptyGerritProject(String path, GerritApi api) throws RestApiException {
        ProjectInput in = new ProjectInput();
        in.name = path;
        in.createEmptyCommit = true;
        api.projects().create(in);
    }

    private Git cloneGerritRepo(String path, Path dest) throws GitAPIException {
        String remote = gerritAuthUrl + "/" + path + ".git";
        CredentialsProvider creds = new UsernamePasswordCredentialsProvider(
                gerritUsername, gerritHttpPassword);
        return Git.cloneRepository()
                .setURI(remote)
                .setDirectory(dest.toFile())
                .setBranch("refs/heads/" + gerritBranch)
                .setCredentialsProvider(creds)
                .call();
    }

    private void fetchGitLabCommit(Git git, String cloneUrl, String sha, String gitlabToken) throws Exception {
        CredentialsProvider gitlabCreds = new UsernamePasswordCredentialsProvider(
                "oauth2", gitlabToken);
        git.remoteAdd().setName("gitlab").setUri(new URIish(cloneUrl)).call();
        git.fetch()
                .setRemote("gitlab")
                .setCredentialsProvider(gitlabCreds)
                .setRefSpecs(new RefSpec(sha + ":" + sha))
                .call();
    }

    private RevCommit parseCommit(Git git, String sha) throws IOException {
        // Resolve the SHA string to an ObjectId
        ObjectId id = git.getRepository().resolve(sha);
        if (id == null) {
            throw new IllegalArgumentException("Invalid commit SHA: " + sha);
        }

        // Parse it into a RevCommit
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            return revWalk.parseCommit(id);
        }
    }

    private String computeChangeId(RevCommit source) throws IOException {
        ObjectId tree = source.getTree().getId();
        ObjectId parent = source.getParentCount() > 0
                ? source.getParent(0).getId()
                : ObjectId.zeroId();
        PersonIdent author = source.getAuthorIdent();
        PersonIdent committer = source.getCommitterIdent();
        ObjectId raw = ChangeIdUtil.computeChangeId(
                tree, parent, author, committer, source.getFullMessage());
        return "I" + raw.name();
    }

    private void squashMergeCommit(Git git, RevCommit source) throws GitAPIException {
        MergeResult result = git.merge()
                .include(source) // the target SHA you want to review
                .setSquash(true) // stage all diffs at once
                .setCommit(false) // we’ll do the commit manually
                .call();

        if (!result.getMergeStatus().isSuccessful()) {
            // Abort by resetting back
            git.reset().setMode(ResetType.HARD).call();
            throw new RuntimeException("Squash-merge failed: " + result.getMergeStatus());
        }
    }

    private void addChangeId(Git git, String changeId, String originalMsg) throws Exception {
        String fullMsg = originalMsg + "\n\nChange-Id: " + changeId;
        git.commit()
                .setAll(true) // commit the staged squash
                .setMessage(fullMsg)
                .call();
    }

    private void pushForReview(Git git, String projectPath) throws GitAPIException, URISyntaxException {
        Iterable<PushResult> results = git.push()
                .setRemote("origin")
                .setRefSpecs(new RefSpec("HEAD:refs/for/" + gerritBranch))
                .setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider(gerritUsername, gerritHttpPassword))
                .call();

        for (PushResult pr : results) {
            for (RemoteRefUpdate rru : pr.getRemoteUpdates()) {
                System.out.printf(
                        "Push %s → %s: %s / %s%n",
                        rru.getSrcRef(), rru.getRemoteName(),
                        rru.getStatus(), // e.g. OK, REJECTED, NOT_ATTEMPTED
                        rru.getMessage() // server-side message if any
                );
            }
        }
    }

}