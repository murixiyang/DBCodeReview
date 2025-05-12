package ic.ac.uk.db_pcr_backend.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.ChangeIdUtil;

import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.api.projects.ProjectInput;
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

    // ** Submit one/several gitlab commits to gerrit */ */
    public String submitForReview(String projectId, String targetSha, String gitlabToken, String username)
            throws Exception {
        String cloneUrl = gitLabSvc.getProjectCloneUrl(projectId, gitlabToken);
        String pathWithNamespace = gitLabSvc.getProjectPathWithNamespace(projectId, gitlabToken);

        GerritApi gerritApi = gerritApiFactory.create(gerritAuthData);
        CredentialsProvider gitlabCreds = new UsernamePasswordCredentialsProvider("oauth2", gitlabToken);
        CredentialsProvider gerritCreds = new UsernamePasswordCredentialsProvider(
                gerritUsername, gerritHttpPassword);

        // Ensure the Gerrit project exists (create if missing)
        ensureGerritProjectExists(gerritApi, pathWithNamespace);

        // Load last submitted SHA from DB
        SubmissionTrackerEntity tracker = submissionTrackerRepo.findByUsernameAndProjectId(username, projectId)
                .orElseGet(() -> new SubmissionTrackerEntity(username, projectId, null));
        String baseSha = tracker.getLastSubmittedSha();

        Path tempDir = Files.createTempDirectory("review-");

        // Clone Gerrit
        Git git = cloneGerritRepo(tempDir, pathWithNamespace, gerritCreds);
        fetchGerritChanges(git, gerritCreds);

        // Clone GitLab
        fetchGitLabCommits(git, cloneUrl, gitlabCreds);

        // Parse the base & target commits
        RevCommit baseCommit = (baseSha != null) ? parseCommit(git, baseSha) : null;
        RevCommit targetCommit = parseCommit(git, targetSha);

        // Checkout to review branch
        String reviewBranch = "review/" + targetSha;
        checkoutReviewBranch(git, reviewBranch, baseCommit);

        // Compute changeId for target
        String changeId = computeChangeId(targetCommit);

        applyDiffAsPatch(git, baseCommit, targetCommit);
        commitWithChangeId(git, changeId, targetCommit.getFullMessage());

        // Push to Gerrit
        PushResult result = pushToGerrit(git, gerritCreds);
        String newGerritSha = extractNewSha(result);

        // --- 7) Record the SHA as the new last submitted
        tracker.setLastSubmittedSha(newGerritSha);
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

    private void createEmptyGerritProject(String path, GerritApi api) throws RestApiException {
        ProjectInput in = new ProjectInput();
        in.name = path;
        in.createEmptyCommit = true;
        api.projects().create(in);
    }

    private Git cloneGerritRepo(Path tempDir, String pathWithNamespace, CredentialsProvider gerritCreds)
            throws GitAPIException {
        return Git.cloneRepository()
                .setURI(gerritAuthUrl + "/" + pathWithNamespace + ".git")
                .setDirectory(tempDir.toFile())
                .setBranch("refs/heads/" + gerritBranch)
                .setCredentialsProvider(gerritCreds)
                .call();
    }

    private void fetchGerritChanges(Git git, CredentialsProvider gerritCreds) throws GitAPIException {
        git.fetch()
                .setRemote("origin")
                .setCredentialsProvider(gerritCreds)
                .setRefSpecs(
                        new RefSpec("+refs/heads/*:refs/remotes/origin/*"),
                        new RefSpec("+refs/changes/*:refs/remotes/gerrit-changes/*"))
                .call();
    }

    private void fetchGitLabCommits(Git git, String cloneUrl, CredentialsProvider gitlabCreds) throws Exception {

        git.remoteAdd().setName("gitlab").setUri(new URIish(cloneUrl)).call();
        git.fetch()
                .setRemote("gitlab")
                .setCredentialsProvider(gitlabCreds)
                .setRefSpecs(new RefSpec("+refs/heads/*:refs/remotes/gitlab/*"))
                .call();
    }

    private void checkoutReviewBranch(Git git, String reviewBranch, RevCommit base) throws Exception {
        if (base != null) {
            git.checkout()
                    .setName(reviewBranch)
                    .setCreateBranch(true)
                    .setStartPoint(base)
                    .call();
        } else {
            git.checkout()
                    .setName(reviewBranch)
                    .setCreateBranch(true)
                    .setStartPoint("refs/heads/" + gerritBranch)
                    .call();
        }

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

    private void applyDiffAsPatch(Git git,
            RevCommit base,
            RevCommit target) throws Exception {
        Repository repo = git.getRepository();

        // If base is null (first review), use HEAD of the branch as base
        RevCommit effectiveBase = (base != null)
                ? base
                : parseCommit(git, "HEAD");

        byte[] patch;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                DiffFormatter df = new DiffFormatter(out)) {
            df.setRepository(repo);
            for (DiffEntry d : df.scan(
                    effectiveBase.getTree(),
                    target.getTree())) {
                df.format(d);
            }
            patch = out.toByteArray();
        }

        try (InputStream in = new ByteArrayInputStream(patch)) {
            git.apply().setPatch(in).call();
        }
    }

    private void commitWithChangeId(Git git, String changeId, String originalMsg) throws Exception {
        String fullMsg = originalMsg + "\n\nChange-Id: " + changeId;
        git.commit()
                .setAll(true) // commit the staged squash
                .setMessage(fullMsg)
                .call();
    }

    private PushResult pushToGerrit(Git git, CredentialsProvider gerritCreds) throws GitAPIException {
        return git.push()
                .setRemote("origin")
                .setRefSpecs(new RefSpec("HEAD:refs/for/" + gerritBranch))
                .setCredentialsProvider(gerritCreds)
                .call()
                .iterator().next();
    }

    private String extractNewSha(PushResult pr) {
        return pr.getRemoteUpdates().stream()
                .map(u -> u.getNewObjectId().getName())
                .findFirst()
                .orElseThrow();
    }

    /** ----- Submission helper ends here ----- */

}