package ic.ac.uk.db_pcr_backend.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.ChangeIdUtil;
import org.gitlab4j.api.models.Project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gerrit.extensions.api.GerritApi;
import com.google.gerrit.extensions.api.projects.ProjectInput;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;
import com.urswolfer.gerrit.client.rest.http.HttpStatusException;

import ic.ac.uk.db_pcr_backend.Constant;
import ic.ac.uk.db_pcr_backend.model.GerritModel.ChangeInfoModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.CommentInfoModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.CommentInputModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.DiffInfoModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.FileInfoModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.ProjectInfoModel;

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
    private final RestTemplate restTemplate;

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
        this.restTemplate = new RestTemplate();
    }

    public String submitForReview(
            String projectId,
            String sha,
            String gitlabToken) throws Exception {

        Project project = this.gitLabSvc.getProjectById(projectId, gitlabToken);
        String cloneUrl = this.gitLabSvc.getProjectCloneUrl(projectId, gitlabToken);
        String defaultBranch = project.getDefaultBranch();
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

        // --- 2) Clone the GitLab repo at that single commit
        Path tempDir = Files.createTempDirectory("review-");
        // use the OAuth token as HTTPS password
        org.eclipse.jgit.transport.CredentialsProvider gitlabCreds = new UsernamePasswordCredentialsProvider("oauth2",
                gitlabToken);

        CloneCommand clone = Git.cloneRepository()
                .setURI(cloneUrl)
                .setDirectory(tempDir.toFile())
                .setCredentialsProvider(gitlabCreds).setBranchesToClone(List.of("refs/heads/" + defaultBranch))
                .setBranch("refs/heads/" + defaultBranch).setDepth(1);

        clone.setCredentialsProvider(gitlabCreds);

        // Run the clone
        try (Git git = clone.call()) {

            // fetch just that SHA
            git.fetch()
                    .setCredentialsProvider(gitlabCreds)
                    .setRefSpecs(new RefSpec(sha + ":" + sha))
                    .call();

            // Now this commit is head
            git.checkout().setName(sha).call();

            // --- 3) Generate a Change-Id for this commit

            // A) Parse the current HEAD commit:
            try (RevWalk revWalk = new RevWalk(git.getRepository())) {
                ObjectId headId = git.getRepository().resolve("HEAD");
                RevCommit commit = revWalk.parseCommit(headId);

                // B) Check existing message for an existing Change-Id to avoid duplicates
                String fullMsg = commit.getFullMessage();
                if (!fullMsg.contains("Change-Id:")) {
                    // C) Generate a Change-Id
                    ObjectId treeId = commit.getTree().getId();
                    ObjectId parentId = commit.getParentCount() > 0
                            ? commit.getParent(0).getId()
                            : ObjectId.zeroId();

                    // 3) Compute the Change-Id SHA
                    PersonIdent author = commit.getAuthorIdent();
                    PersonIdent committer = commit.getCommitterIdent();
                    ObjectId rawId = ChangeIdUtil.computeChangeId(
                            treeId, parentId, author, committer, fullMsg);

                    // 4) Build the “I…” string
                    String changeId = "I" + rawId.name();
                    String amendedMsg = fullMsg + "\n\n" + "Change-Id: " + changeId;

                    // D) Set the new message
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
            String remote = gerritAuthUrl + "/" + pathWithNamespace + ".git";
            CredentialsProvider gerritCreds = new UsernamePasswordCredentialsProvider(
                    gerritUsername, gerritHttpPassword);
            RefSpec refSpec = new RefSpec("HEAD:refs/for/" + gerritBranch);

            // Push the new head to Gerrit
            Iterable<PushResult> results = git.push()
                    .setRemote(remote)
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

        }

        // --- 5) Look up the new Gerrit Change by commit SHA
        System.out.println("DBLOG: Look up Gerrit Change by commit SHA: " + sha);

        try {
            List<ChangeInfo> changes = gerritApi.changes()
                    .query("commit:" + sha)
                    .withQuery("status=open")
                    .get();

            System.out.println("DBLOG: Found " + changes.size() + " Gerrit changes for commit SHA: " + sha);

            // return numeric Change-Id as a string
            return String.valueOf(changes.get(0)._number);
        } catch (RestApiException e) {
            System.out.println("DBLOG: No Gerrit change found for commit SHA: " + sha);
            throw new IllegalStateException("Gerrit change not found for " + sha);
        }

    }

    public Map<String, ProjectInfoModel> getProjectList() {
        String endPoint = "/projects";

        return fetchGerritMapData(endPoint, ProjectInfoModel.class, false);
    }

    public List<ChangeInfoModel> getChangesWithQuery(String query) {
        String endPoint = "/changes?q=" + query;

        return fetchGerritListData(endPoint, ChangeInfoModel[].class, false);
    }

    public Map<String, FileInfoModel> getModifiedFileInChange(String changeId, String revisionId) {
        String endPoint = "/changes/" + changeId + "/revisions/" + revisionId + "/files";

        return fetchGerritMapData(endPoint, FileInfoModel.class, false);
    }

    public DiffInfoModel getDiffInFile(String changeId, String revisionId, String filePath) {
        String endPoint = "/changes/" + changeId + "/revisions/" + revisionId + "/files/" + filePath + "/diff";

        return fetchGerritData(endPoint, DiffInfoModel.class, false);
    }

    public Map<String, CommentInfoModel[]> getAllDraftComments(String changeId, String revisionId) {
        String endPoint = "/changes/" + changeId + "/revisions/" + revisionId + "/drafts";

        return fetchGerritMapData(endPoint, CommentInfoModel[].class, true);
    }

    public Map<String, CommentInfoModel[]> getAllComments(String changeId, String revisionId) {
        String endPoint = "/changes/" + changeId + "/revisions/" + revisionId + "/comments";

        return fetchGerritMapData(endPoint, CommentInfoModel[].class, false);
    }

    public ResponseEntity<CommentInfoModel> updateDraftComment(String changeId, String revisionId,
            CommentInputModel commentInput) {

        String endPoint = Constant.getGerritBaseUrl(true) + "/changes/" + changeId +
                "/revisions/" + revisionId + "/drafts/" + commentInput.id;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(Constant.ADMIN_USERNAME, Constant.ADMIN_PASSWORD);

        HttpEntity<CommentInputModel> requestEntity = new HttpEntity<>(commentInput, headers);

        try {

            ResponseEntity<String> response = restTemplate.exchange(
                    endPoint,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class);
            String json = CommonFunctionService.trimJson(response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            CommentInfoModel result = mapper.readValue(json, CommentInfoModel.class);

            return ResponseEntity.status(response.getStatusCode()).body(result);

        } catch (IOException e) {
            System.out.println("ERROR: Failed to put draft comment to Gerrit at endpoint: " + endPoint);
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<CommentInfoModel> putDraftComment(String changeId, String revisionId,
            CommentInputModel commentInput) {

        String endPoint = Constant.getGerritBaseUrl(true) + "/changes/" + changeId +
                "/revisions/" + revisionId + "/drafts";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(Constant.ADMIN_USERNAME, Constant.ADMIN_PASSWORD);

        HttpEntity<CommentInputModel> requestEntity = new HttpEntity<>(commentInput, headers);

        try {

            ResponseEntity<String> response = restTemplate.exchange(
                    endPoint,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class);
            String json = CommonFunctionService.trimJson(response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            CommentInfoModel result = mapper.readValue(json, CommentInfoModel.class);

            return ResponseEntity.status(response.getStatusCode()).body(result);

        } catch (IOException e) {
            System.out.println("ERROR: Failed to put draft comment to Gerrit at endpoint: " + endPoint);
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<String> deleteDraftComment(String changeId, String revisionId, String draftId) {
        String endPoint = Constant.getGerritBaseUrl(true) + "/changes/" + changeId +
                "/revisions/" + revisionId + "/drafts/" + draftId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(Constant.ADMIN_USERNAME, Constant.ADMIN_PASSWORD);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    endPoint,
                    HttpMethod.DELETE,
                    entity,
                    String.class);

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            System.out.println("ERROR: Failed to delete draft comment from Gerrit at endpoint: " + endPoint);
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    private <T> T fetchGerritData(String endpoint, Class<T> dataClass, Boolean needAuth) {
        try {
            String url = Constant.getGerritBaseUrl(needAuth) + endpoint;

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getBody() == null) {
                return null;
            }

            String json = CommonFunctionService.trimJson(response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, dataClass);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to fetch data from Gerrit at endpoint: " + endpoint);
            e.printStackTrace();
            return null;
        }
    }

    private <T> List<T> fetchGerritListData(String endpoint, Class<T[]> dataClass, Boolean needAuth) {
        try {
            String url = Constant.getGerritBaseUrl(needAuth) + endpoint;

            HttpHeaders headers = new HttpHeaders();
            if (needAuth) {
                headers.setBasicAuth(Constant.ADMIN_USERNAME, Constant.ADMIN_PASSWORD);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() == null) {
                return Collections.emptyList();
            }

            String json = CommonFunctionService.trimJson(response.getBody());

            System.out.println("json: " + json);

            ObjectMapper mapper = new ObjectMapper();
            T[] data = mapper.readValue(json, dataClass);
            return Arrays.asList(data);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to fetch data from Gerrit at endpoint: " + endpoint);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private <T> Map<String, T> fetchGerritMapData(String endpoint, Class<T> dataClass, Boolean needAuth) {
        try {
            String url = Constant.getGerritBaseUrl(needAuth) + endpoint;

            HttpHeaders headers = new HttpHeaders();
            if (needAuth) {
                headers.setBasicAuth(Constant.ADMIN_USERNAME, Constant.ADMIN_PASSWORD);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() == null) {
                return Collections.emptyMap();
            }

            String json = CommonFunctionService.trimJson(response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            Map<String, T> dataMap = mapper.readValue(
                    json,
                    mapper.getTypeFactory().constructMapType(Map.class, String.class, dataClass));

            return dataMap;
        } catch (IOException e) {
            System.err.println("ERROR: Failed to fetch data from Gerrit at endpoint: " + endpoint);
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

}
