package ic.ac.uk.db_pcr_backend.service;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.eclipse.jgit.api.Git;
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

    public String submitForReview(
            String projectId,
            String sha,
            String gitlabToken) throws Exception {

        String returnChangeId = "";

        Project project = this.gitLabSvc.getProjectById(projectId, gitlabToken);
        String cloneUrl = this.gitLabSvc.getProjectCloneUrl(projectId, gitlabToken);
        String pathWithNamespace = project.getPathWithNamespace();

        // --- 1) Ensure the Gerrit project exists (create if missing)
        GerritApi gerritApi = gerritApiFactory.create(gerritAuthData);
        try {
            gerritApi.projects().name(pathWithNamespace).get();
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof HttpStatusException hse && hse.getStatusCode() == 404) {
                // Project not found, create it
                System.out.println("DBLOG: Gerrit project not exists. Create it: " + pathWithNamespace);
                ProjectInput in = new ProjectInput();
                in.name = pathWithNamespace;
                in.createEmptyCommit = true;
                gerritApi.projects().create(in);
            } else {
                System.out.println("ERROR: Failed to check Gerrit project: " + e.getMessage());
                throw e;
            }
        }

        // --- 2) Clone Gerrit repo
        Path tempDir = Files.createTempDirectory("review-");
        String gerritRemote = gerritAuthUrl + "/" + pathWithNamespace + ".git";
        CredentialsProvider gerritCreds = new UsernamePasswordCredentialsProvider(
                gerritUsername, gerritHttpPassword);
        CredentialsProvider gitlabCreds = new UsernamePasswordCredentialsProvider("oauth2",
                gitlabToken);

        Git git = Git.cloneRepository()
                .setURI(gerritRemote)
                .setDirectory(tempDir.toFile())
                .setBranch("refs/heads/" + gerritBranch)
                .setCredentialsProvider(gerritCreds)
                .call();

        // --- 3) Add Gitlab repo as a remote and fetch commit
        git.remoteAdd()
                .setName("gitlab")
                .setUri(new URIish(cloneUrl))
                .call();

        // Fetch only the commit we need
        git.fetch()
                .setRemote("gitlab")
                .setCredentialsProvider(gitlabCreds)
                .setRefSpecs(new RefSpec(sha + ":" + sha))
                .call();

        // --- 4) Cherry-pick the GitLab commit onto the Gerrit branch
        ObjectId commitId = git.getRepository().resolve(sha);
        RevCommit commit = new RevWalk(git.getRepository()).parseCommit(commitId);
        git.cherryPick()
                .include(commit)
                .call();

        // --- 5) Generate a Change-Id for this commit

        // Parse the current HEAD commit:
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            ObjectId headId = git.getRepository().resolve("HEAD");
            RevCommit headCommit = revWalk.parseCommit(headId);

            // Check existing message for an existing Change-Id to avoid duplicates
            String fullMsg = headCommit.getFullMessage();
            if (!fullMsg.contains("Change-Id:")) {
                // Generate a Change-Id
                ObjectId treeId = headCommit.getTree().getId();
                ObjectId parentId = headCommit.getParentCount() > 0
                        ? headCommit.getParent(0).getId()
                        : ObjectId.zeroId();

                PersonIdent author = headCommit.getAuthorIdent();
                PersonIdent committer = headCommit.getCommitterIdent();
                ObjectId rawId = ChangeIdUtil.computeChangeId(
                        treeId, parentId, author, committer, fullMsg);

                String changeId = "I" + rawId.name();
                returnChangeId = changeId;
                String amendedMsg = fullMsg + "\n\n" + "Change-Id: " + changeId;

                // Set the new message
                git.commit().setAmend(true).setMessage(amendedMsg).call();

            }

            ObjectId newHead = git.getRepository().resolve("HEAD");
            try (RevWalk rw2 = new RevWalk(git.getRepository())) {
                RevCommit newCommit = rw2.parseCommit(newHead);
                System.out.println("DBLOG: Amended commit msg:\n"
                        + newCommit.getFullMessage());
            }
        }

        // --- 4) Push into Gerrit’s refs/for/<branch>

        RefSpec refSpec = new RefSpec("HEAD:refs/for/" + gerritBranch);

        // Push the new head to Gerrit
        Iterable<PushResult> results = git.push()
                .setRemote(gerritRemote)
                .setRefSpecs(refSpec)
                .setCredentialsProvider(gerritCreds)
                .call();

        for (PushResult pr : results) {
            for (RemoteRefUpdate rru : pr.getRemoteUpdates()) {
                System.out.printf(
                        "DBLOG: push %s → %s : %s %n    server says: %s%n",
                        rru.getSrcRef(), rru.getRemoteName(),
                        rru.getStatus(),
                        rru.getMessage());
            }
        }

        // --- 5) Return change id
        return returnChangeId;

    }

}
