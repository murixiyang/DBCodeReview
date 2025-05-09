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
import org.eclipse.jgit.transport.RefSpec;
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

    public GerritService(
            GitLabService gitLabService,
            @Value("${gerrit.url}") String gerritHttpUrl,
            @Value("${gerrit.auth.url}") String gerritAuthUrl,
            @Value("${gerrit.username}") String gerritUsername,
            @Value("${gerrit.password}") String gerritHttpPassword,
            @Value("${gerrit.branch:master}") String gerritBranch) {

        this.gitLabSvc = gitLabService;
        this.gerritHttpUrl = gerritHttpUrl;
        this.gerritAuthUrl = gerritAuthUrl;
        this.gerritUsername = gerritUsername;
        this.gerritHttpPassword = gerritHttpPassword;
        this.gerritBranch = gerritBranch;

        this.gerritAuthData = new GerritAuthData.Basic(
                gerritHttpUrl, gerritUsername, gerritHttpPassword);
        this.gerritApiFactory = new GerritRestApiFactory();
    }

    public String submitForReview(String projectId, String sha, String gitlabToken) throws Exception {
        String cloneUrl = gitLabSvc.getProjectCloneUrl(projectId, gitlabToken);
        String pathWithNamespace = gitLabSvc.getProjectPathWithNamespace(projectId, gitlabToken);

        GerritApi gerritApi = gerritApiFactory.create(gerritAuthData);

        // --- 1) Ensure the Gerrit project exists (create if missing)
        ensureGerritProjectExists(gerritApi, pathWithNamespace);

        // --- 2) Clone Gerrit repo
        Path tempDir = Files.createTempDirectory("review-");
        Git git = cloneGerritRepo(pathWithNamespace, tempDir);

        // --- 3) Add Gitlab repo as a remote and fetch commit
        fetchGitLabCommit(git, cloneUrl, sha, gitlabToken);

        // Store orignial msg before cherry-picking
        RevCommit source = parseCommit(git, sha);
        String changeId = computeChangeId(source);

        // --- 4) Check if the commit is already under review
        Optional<ChangeInfo> existing = findExistingChange(gerritApi, changeId);
        if (existing.isPresent()) {
            // already under review — return its Change-Id
            System.out.println("DBLOG: Found existing change for commit " + sha);

            System.out.println("DBLOG: Change-Id: " + existing.get().changeId);
            return existing.get().changeId;
        }

        // --- 5) Add Change-Id to this commit
        squashMergeCommit(git, source);
        addChangeId(git, changeId, source.getFullMessage());

        // --- 6) Push into Gerrit’s refs/for/<branch>
        pushForReview(git, pathWithNamespace);

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
        String remote = gerritAuthUrl + "/" + projectPath + ".git";
        RefSpec spec = new RefSpec("HEAD:refs/for/" + gerritBranch);
        CredentialsProvider creds = new UsernamePasswordCredentialsProvider(
                gerritUsername, gerritHttpPassword);
        git.push().setRemote(remote).setRefSpecs(spec).setCredentialsProvider(creds).call();
    }

}