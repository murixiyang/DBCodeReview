package ic.ac.uk.db_pcr_backend.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.ChangeIdUtil;
import org.gitlab4j.api.models.Project;

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

        // 2) Check for an existing change by this exact commit
        Optional<ChangeInfo> existing = findExistingChange(gerritApi, pathWithNamespace, sha);
        if (existing.isPresent()) {
            // already under review — return its Change-Id
            System.out.println("DBLOG: Found existing change for commit " + sha);

            System.out.println("DBLOG: Change-Id: " + existing.get().changeId);
            return existing.get().changeId;
        }

        // --- 3) Clone Gerrit repo
        Path tempDir = Files.createTempDirectory("review-");
        Git git = cloneGerritRepo(pathWithNamespace, tempDir);

        // --- 4) Add Gitlab repo as a remote and fetch commit
        fetchGitLabCommit(git, cloneUrl, sha, gitlabToken);
        // Fetch only the commit we need
        cherryPickCommit(git, sha);

        // --- 5) Generate a Change-Id for this commit
        String changeId = generateChangeIdIfMissing(git);

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
            String project,
            String commitSha)
            throws RestApiException {
        String raw = "project:" + project + " commit:" + commitSha;
        // Encode the query
        String encoded = URLEncoder.encode(raw, StandardCharsets.UTF_8);

        List<ChangeInfo> results = api.changes()
                .query(encoded)
                .get();
        return results.isEmpty()
                ? Optional.empty()
                : Optional.of(results.get(0));
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

    private void cherryPickCommit(Git git, String sha) throws Exception {
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            RevCommit commit = revWalk.parseCommit(git.getRepository().resolve(sha));
            git.cherryPick().include(commit).call();
        }
    }

    private String generateChangeIdIfMissing(Git git) throws Exception {
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            RevCommit head = revWalk.parseCommit(git.getRepository().resolve("HEAD"));
            String msg = head.getFullMessage();
            if (!msg.contains("Change-Id:")) {
                String changeId = computeChangeId(head);
                git.commit().setAmend(true).setMessage(msg + "\n\nChange-Id: " + changeId).call();
                return changeId;
            }
            return "";
        }

    }

    private String computeChangeId(RevCommit head) throws IOException {
        ObjectId tree = head.getTree().getId();
        ObjectId parent = head.getParentCount() > 0
                ? head.getParent(0).getId()
                : ObjectId.zeroId();
        PersonIdent author = head.getAuthorIdent();
        PersonIdent committer = head.getCommitterIdent();
        ObjectId raw = ChangeIdUtil.computeChangeId(tree, parent, author, committer, head.getFullMessage());
        return "I" + raw.name();
    }

    private void pushForReview(Git git, String projectPath) throws GitAPIException, URISyntaxException {
        String remote = gerritAuthUrl + "/" + projectPath + ".git";
        RefSpec spec = new RefSpec("HEAD:refs/for/" + gerritBranch);
        CredentialsProvider creds = new UsernamePasswordCredentialsProvider(
                gerritUsername, gerritHttpPassword);
        git.push().setRemote(remote).setRefSpecs(spec).setCredentialsProvider(creds).call();
    }

    // public String submitForReview(
    // String projectId,
    // String sha,
    // String gitlabToken) throws Exception {

    // String returnChangeId = "";

    // Project project = this.gitLabSvc.getProjectById(projectId, gitlabToken);
    // String cloneUrl = this.gitLabSvc.getProjectCloneUrl(projectId, gitlabToken);
    // String pathWithNamespace = project.getPathWithNamespace();

    // // --- 1) Ensure the Gerrit project exists (create if missing)
    // GerritApi gerritApi = gerritApiFactory.create(gerritAuthData);
    // try {
    // gerritApi.projects().name(pathWithNamespace).get();
    // } catch (RuntimeException e) {
    // Throwable cause = e.getCause();
    // if (cause instanceof HttpStatusException hse && hse.getStatusCode() == 404) {
    // // Project not found, create it
    // System.out.println("DBLOG: Gerrit project not exists. Create it: " +
    // pathWithNamespace);
    // ProjectInput in = new ProjectInput();
    // in.name = pathWithNamespace;
    // in.createEmptyCommit = true;
    // gerritApi.projects().create(in);
    // } else {
    // System.out.println("ERROR: Failed to check Gerrit project: " +
    // e.getMessage());
    // throw e;
    // }
    // }

    // // --- 2) Clone Gerrit repo
    // Path tempDir = Files.createTempDirectory("review-");
    // String gerritRemote = gerritAuthUrl + "/" + pathWithNamespace + ".git";
    // CredentialsProvider gerritCreds = new UsernamePasswordCredentialsProvider(
    // gerritUsername, gerritHttpPassword);
    // CredentialsProvider gitlabCreds = new
    // UsernamePasswordCredentialsProvider("oauth2",
    // gitlabToken);

    // Git git = Git.cloneRepository()
    // .setURI(gerritRemote)
    // .setDirectory(tempDir.toFile())
    // .setBranch("refs/heads/" + gerritBranch)
    // .setCredentialsProvider(gerritCreds)
    // .call();

    // // --- 3) Add Gitlab repo as a remote and fetch commit
    // git.remoteAdd()
    // .setName("gitlab")
    // .setUri(new URIish(cloneUrl))
    // .call();

    // // Fetch only the commit we need
    // git.fetch()
    // .setRemote("gitlab")
    // .setCredentialsProvider(gitlabCreds)
    // .setRefSpecs(new RefSpec(sha + ":" + sha))
    // .call();

    // // --- 4) Cherry-pick the GitLab commit onto the Gerrit branch
    // ObjectId commitId = git.getRepository().resolve(sha);
    // RevCommit commit = new RevWalk(git.getRepository()).parseCommit(commitId);
    // git.cherryPick()
    // .include(commit)
    // .call();

    // // --- 5) Generate a Change-Id for this commit

    // // Parse the current HEAD commit:
    // try (RevWalk revWalk = new RevWalk(git.getRepository())) {
    // ObjectId headId = git.getRepository().resolve("HEAD");
    // RevCommit headCommit = revWalk.parseCommit(headId);

    // // Check existing message for an existing Change-Id to avoid duplicates
    // String fullMsg = headCommit.getFullMessage();
    // if (!fullMsg.contains("Change-Id:")) {
    // // Generate a Change-Id
    // ObjectId treeId = headCommit.getTree().getId();
    // ObjectId parentId = headCommit.getParentCount() > 0
    // ? headCommit.getParent(0).getId()
    // : ObjectId.zeroId();

    // PersonIdent author = headCommit.getAuthorIdent();
    // PersonIdent committer = headCommit.getCommitterIdent();
    // ObjectId rawId = ChangeIdUtil.computeChangeId(
    // treeId, parentId, author, committer, fullMsg);

    // String changeId = "I" + rawId.name();
    // returnChangeId = changeId;
    // String amendedMsg = fullMsg + "\n\n" + "Change-Id: " + changeId;

    // // Set the new message
    // git.commit().setAmend(true).setMessage(amendedMsg).call();

    // }

    // ObjectId newHead = git.getRepository().resolve("HEAD");
    // try (RevWalk rw2 = new RevWalk(git.getRepository())) {
    // RevCommit newCommit = rw2.parseCommit(newHead);
    // System.out.println("DBLOG: Amended commit msg:\n"
    // + newCommit.getFullMessage());
    // }
    // }

    // // --- 4) Push into Gerrit’s refs/for/<branch>

    // RefSpec refSpec = new RefSpec("HEAD:refs/for/" + gerritBranch);

    // // Push the new head to Gerrit
    // Iterable<PushResult> results = git.push()
    // .setRemote(gerritRemote)
    // .setRefSpecs(refSpec)
    // .setCredentialsProvider(gerritCreds)
    // .call();

    // for (PushResult pr : results) {
    // for (RemoteRefUpdate rru : pr.getRemoteUpdates()) {
    // System.out.printf(
    // "DBLOG: push %s → %s : %s %n server says: %s%n",
    // rru.getSrcRef(), rru.getRemoteName(),
    // rru.getStatus(),
    // rru.getMessage());
    // }
    // }

    // // --- 5) Return change id
    // return returnChangeId;

    // }

}
