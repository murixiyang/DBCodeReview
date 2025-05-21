package ic.ac.uk.db_pcr_backend.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
import com.google.gerrit.extensions.api.changes.DraftInput;
import com.google.gerrit.extensions.api.projects.ProjectInput;
import com.google.gerrit.extensions.client.Comment;
import com.google.gerrit.extensions.client.Side;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.CommentInfo;
import com.google.gerrit.extensions.common.FileInfo;
import com.google.gerrit.extensions.restapi.BinaryResult;
import com.google.gerrit.extensions.restapi.RestApiException;
import com.urswolfer.gerrit.client.rest.GerritAuthData;
import com.urswolfer.gerrit.client.rest.GerritRestApiFactory;
import com.urswolfer.gerrit.client.rest.http.HttpStatusException;

import ic.ac.uk.db_pcr_backend.dto.gerritdto.CommentInfoDto;
import ic.ac.uk.db_pcr_backend.dto.gerritdto.CommentInputDto;
import ic.ac.uk.db_pcr_backend.entity.ChangeRequestEntity;
import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.entity.SubmissionTrackerEntity;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.GitlabCommitRepo;
import ic.ac.uk.db_pcr_backend.repository.SubmissionTrackerRepo;

@Service
public class GerritService {
    private final RestTemplateBuilder builder;

    @Autowired
    private GitLabService gitLabSvc;

    @Autowired
    private SubmissionTrackerService submissionTrackerSvc;

    @Autowired
    private ChangeRequestService changeRequestSvc;

    @Autowired
    private GitlabCommitRepo commitRepo;

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    @Autowired
    private SubmissionTrackerRepo submissionTrackerRepo;

    private final String gerritAuthUrl;
    private final String gerritUsername;
    private final String gerritHttpPassword;
    private final String gerritBranch;

    private final GerritAuthData.Basic gerritAuthData;
    private final GerritRestApiFactory gerritApiFactory;
    private final GerritApi gerritApi;

    public GerritService(
            RestTemplateBuilder builder,
            @Value("${gerrit.url}") String gerritHttpUrl,
            @Value("${gerrit.auth.url}") String gerritAuthUrl,
            @Value("${gerrit.username}") String gerritUsername,
            @Value("${gerrit.password}") String gerritHttpPassword,
            @Value("${gerrit.branch:master}") String gerritBranch) {

        this.builder = builder;

        this.gerritAuthUrl = gerritAuthUrl;
        this.gerritUsername = gerritUsername;
        this.gerritHttpPassword = gerritHttpPassword;
        this.gerritBranch = gerritBranch;

        this.gerritAuthData = new GerritAuthData.Basic(
                gerritHttpUrl, gerritUsername, gerritHttpPassword);
        this.gerritApiFactory = new GerritRestApiFactory();

        this.gerritApi = gerritApiFactory.create(gerritAuthData);
    }

    public String getGerritChangeIdByCommitId(Long commitId) throws IllegalArgumentException {
        System.out.println("Service: GerritService.getGerritChangeIdByCommitId");

        // Find Commit
        GitlabCommitEntity commit = commitRepo.findById(commitId).orElseThrow(() -> new IllegalArgumentException(
                "Unknown commit id " + commitId));

        // Find the related ChangeRequest
        // Will find multiple, each related to 1 reviewer, but all have the same
        // changeId
        List<ChangeRequestEntity> changeRequests = changeRequestRepo.findByCommit(commit);

        if (changeRequests == null || changeRequests.size() == 0) {
            // Throw exception
            throw new IllegalArgumentException("No change requests found for commit id " + commitId);
        }

        return changeRequests.get(0).getGerritChangeId();

    }

    // * Fetch commits list using repo Path */
    public List<ChangeInfo> getCommitsFromProjectPath(String path) throws Exception {
        System.out.println("Service: GerritService.getCommitsFromProjectPath");

        return gerritApi.changes()
                .query("project:" + path)
                .get();
    }

    // * Get changed file names in a gerrit change */
    public List<String> getChangedFileNames(String changeId) throws Exception {
        System.out.println("Service: GerritService.getChangedFileNames");

        Map<String, FileInfo> files = gerritApi.changes()
                .id(changeId)
                .revision("current")
                .files();

        // Exclude COMMIT_MSG file

        return files.keySet().stream()
                .filter(fileName -> !fileName.equals("/COMMIT_MSG"))
                .collect(Collectors.toList());
    }

    // * Get Before and After file content */
    public Map<String, String[]> getChangedFileContent(String changeId) throws Exception {
        System.out.println("Service: GerritService.getFileContent");

        List<String> fileNames = getChangedFileNames(changeId);

        String previousChangeId = null;

        // Find ChangeRequest by changeId
        List<ChangeRequestEntity> changeRequests = changeRequestRepo.findByGerritChangeId(changeId);
        if (changeRequests == null || changeRequests.size() == 0) {
            // Throw exception
            throw new IllegalArgumentException("No change requests found for changeId " + changeId);
        }

        // All of the change requests have the same commit Id
        Long commitId = changeRequests.get(0).getCommit().getId();

        // Find the related commit
        GitlabCommitEntity commit = commitRepo.findById(commitId).orElseThrow(() -> new IllegalArgumentException(
                "Unknown commit id " + commitId));

        // Use commitId to find the submissionTracker and get previous submission
        SubmissionTrackerEntity submission = submissionTrackerRepo.findBySubmittedCommit(commit)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown commit id " + commitId));

        SubmissionTrackerEntity previousSubmission = submission.getPreviousSubmission();

        // If not the first commit, find the previous commit
        if (previousSubmission != null) {
            GitlabCommitEntity previousCommitEntity = previousSubmission.getSubmittedCommit();
            List<ChangeRequestEntity> previousChangeRequest = changeRequestRepo.findByCommit(previousCommitEntity);

            // They all share the same changeId
            if (previousChangeRequest != null && previousChangeRequest.size() > 0) {
                previousChangeId = previousChangeRequest.get(0).getGerritChangeId();
            }
        }

        Map<String, String[]> fileContentMap = new HashMap<String, String[]>();

        for (String fileName : fileNames) {
            BinaryResult oldFile = null;

            String[] content = new String[2];
            content[0] = "";
            content[1] = "";

            // If teher is a previous changeId, get the old file
            if (previousChangeId != null) {
                // Get old file
                try {
                    oldFile = gerritApi.changes()
                            .id(previousChangeId)
                            .revision("current")
                            .file(fileName).content();

                    content[0] = oldFile != null ? new String(
                            Base64.getDecoder().decode(oldFile.asString()),
                            StandardCharsets.UTF_8) : "";

                } catch (RestApiException e) {
                    // Handle the case where the file does not exist in the previous change
                    System.out.println("File " + fileName + " does not exist in previous change " + previousChangeId);
                }
            }

            try {
                // Get new file
                BinaryResult newFile = gerritApi.changes()
                        .id(changeId)
                        .revision("current")
                        .file(fileName).content();

                content[1] = new String(
                        Base64.getDecoder().decode(newFile.asString()),
                        StandardCharsets.UTF_8);

            } catch (RestApiException e) {
                // Handle the case where the file does not exist in the current change
                System.out.println("File " + fileName + " does not exist in current change " + changeId);
            }

            fileContentMap.put(fileName, content);
        }

        return fileContentMap;
    }

    public String fetchRawPatch(String changeId, String revisionId) {
        System.out.println("Service: GerritService.fetchRawPatch");

        String url = "/changes/" + URLEncoder.encode(changeId, StandardCharsets.UTF_8)
                + "/revisions/" + revisionId
                + "/patch?download=true";

        RestTemplate rest = builder
                .rootUri(gerritAuthUrl)
                .basicAuthentication(gerritUsername, gerritHttpPassword)
                .build();

        String rawB64 = rest.getForObject(url, String.class);
        // Gerrit may still prepend the XSSI guard; strip up to the first letter of the
        // patch
        String stripped = rawB64.replaceFirst("(?s)^.*?(?=RnVmZik|ZGlmZik|diff)", "");

        // Now decode the base64 to get the real unified diff text
        byte[] decoded = Base64.getDecoder().decode(stripped);
        String patch = new String(decoded, StandardCharsets.UTF_8);

        int idx = patch.indexOf("diff --git");
        if (idx > 0) {
            patch = patch.substring(idx);
        }

        return patch;
    }

    public List<CommentInfoDto> getGerritChangeComments(String gerritChangeId) throws RestApiException {
        System.out.println("Service: GerritService.getGerritChangeComments");

        // 1) fetch the map: filePath → [ CommentInfo, … ]
        Map<String, List<CommentInfo>> commentMap = gerritApi.changes().id(gerritChangeId).comments();

        // 2) flatten but carry along the key (filePath)
        List<CommentInfoDto> comments = commentMap.entrySet().stream()
                .flatMap(entry -> {
                    String filePath = entry.getKey();
                    return entry.getValue().stream()
                            .map(ci -> CommentInfoDto.fromGerritType(filePath, ci));
                })
                .collect(Collectors.toList());

        return comments;
    }

    public List<CommentInfoDto> getGerritChangeDraftComments(String gerritChangeId) throws RestApiException {
        System.out.println("Service: GerritService.getGerritChangeDraftComments");

        // 1) fetch the map: filePath → [ CommentInfo, … ]
        Map<String, List<CommentInfo>> draftMap = gerritApi.changes().id(gerritChangeId).drafts();

        // 2) flatten but carry along the key (filePath)
        List<CommentInfoDto> drafts = draftMap.entrySet().stream()
                .flatMap(entry -> {
                    String filePath = entry.getKey();
                    return entry.getValue().stream()
                            .map(ci -> CommentInfoDto.fromGerritType(filePath, ci));
                })
                .collect(Collectors.toList());

        return drafts;
    }

    public CommentInfoDto postGerritDraft(String gerritChangeId, CommentInputDto commentInput) throws RestApiException {
        System.out.println("Service: GerritService.postGerritComment");

        System.out.println("DBLOG: received comment: " + commentInput);

        DraftInput draft = createDraftInput(commentInput);

        CommentInfo created = gerritApi
                .changes()
                .id(gerritChangeId)
                .revision("current")
                .createDraft(draft)
                .get();

        return CommentInfoDto.fromGerritType(commentInput.getPath(), created);
    }

    private DraftInput createDraftInput(CommentInputDto commentInput) {
        System.out.println("Service: GerritService.createDraftInput");

        DraftInput draft = new DraftInput();
        if (commentInput.getId() != null) {
            draft.id = commentInput.getId();
        }
        draft.path = commentInput.getPath();
        draft.side = Side.valueOf(commentInput.getSide());
        draft.line = commentInput.getLine();
        draft.message = commentInput.getMessage();
        draft.inReplyTo = commentInput.getInReplyTo();

        System.out.println("DBLOG: created draft: " + draft);

        return draft;
    }

    public CommentInfoDto updateGerritDraft(String gerritChangeId, CommentInputDto commentInput)
            throws RestApiException {
        System.out.println("Service: GerritService.updateGerritDraft");

        CommentInfo updated = gerritApi.changes()
                .id(gerritChangeId)
                .revision("current")
                .draft(commentInput.getId()).update(createDraftInput(commentInput));

        return CommentInfoDto.fromGerritType(commentInput.getPath(), updated);
    }

    public void deleteGerritDraft(String gerritChangeId, CommentInputDto commentInput)
            throws RestApiException {
        System.out.println("Service: GerritService.deleteGerritDraft");

        gerritApi.changes()
                .id(gerritChangeId)
                .revision("current")
                .draft(commentInput.getId()).delete();

    }

    public void submitDraftAsComment(String gerritChangeId, String message) throws RestApiException {
        System.out.println("Service: GerritService.submitDraftAsComment");

    }

    /* ----------- SUBMIT FOR REVIEW ---------------- */

    // ** Submit one/several gitlab commits to gerrit */ */
    public String submitForReview(String gitlabProjectId, GitlabCommitEntity targetCommitEntity, String gitlabToken,
            String username)
            throws Exception {
        System.out.println("Service: GerritService.submitForReview");

        String targetSha = targetCommitEntity.getGitlabCommitId();

        String cloneUrl = gitLabSvc.getProjectCloneUrl(gitlabProjectId, gitlabToken);
        String pathWithNamespace = gitLabSvc.getProjectPathWithNamespace(gitlabProjectId, gitlabToken);

        GerritApi gerritApi = gerritApiFactory.create(gerritAuthData);
        CredentialsProvider gitlabCreds = new UsernamePasswordCredentialsProvider("oauth2", gitlabToken);
        CredentialsProvider gerritCreds = new UsernamePasswordCredentialsProvider(
                gerritUsername, gerritHttpPassword);

        // Ensure the Gerrit project exists (create if missing)
        ensureGerritProjectExists(gerritApi, pathWithNamespace);

        Long gitlabProjectIdLong = Long.parseLong(gitlabProjectId);
        // Load last submitted SHA from DB
        SubmissionTrackerEntity previousSubmission = submissionTrackerSvc.getPreviousSubmission(username,
                gitlabProjectIdLong);
        String baseSha = previousSubmission != null
                ? previousSubmission.getSubmittedGerritSha()
                : null;

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

        // --- Record the SHA as the new last submitted
        submissionTrackerSvc.recordSubmission(username, gitlabProjectIdLong, previousSubmission, newGerritSha,
                targetCommitEntity);

        // --- Record the change request
        changeRequestSvc.insertNewChangeRequest(gitlabProjectIdLong, targetSha, username, newGerritSha);

        return changeId;
    }

    private void ensureGerritProjectExists(GerritApi api, String path) throws RestApiException {
        System.out.println("Service: GerritService.ensureGerritProjectExists");

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
        System.out.println("Service: GerritService.createEmptyGerritProject");

        ProjectInput in = new ProjectInput();
        in.name = path;
        in.createEmptyCommit = true;
        api.projects().create(in);
    }

    private Git cloneGerritRepo(Path tempDir, String pathWithNamespace, CredentialsProvider gerritCreds)
            throws GitAPIException {

        System.out.println("Service: GerritService.cloneGerritRepo");

        return Git.cloneRepository()
                .setURI(gerritAuthUrl + "/" + pathWithNamespace + ".git")
                .setDirectory(tempDir.toFile())
                .setBranch("refs/heads/" + gerritBranch)
                .setCredentialsProvider(gerritCreds)
                .call();
    }

    private void fetchGerritChanges(Git git, CredentialsProvider gerritCreds) throws GitAPIException {

        System.out.println("Service: GerritService.fetchGerritChanges");

        git.fetch()
                .setRemote("origin")
                .setCredentialsProvider(gerritCreds)
                .setRefSpecs(
                        new RefSpec("+refs/heads/*:refs/remotes/origin/*"),
                        new RefSpec("+refs/changes/*:refs/remotes/gerrit-changes/*"))
                .call();
    }

    private void fetchGitLabCommits(Git git, String cloneUrl, CredentialsProvider gitlabCreds) throws Exception {

        System.out.println("Service: GerritService.fetchGitLabCommits");

        git.remoteAdd().setName("gitlab").setUri(new URIish(cloneUrl)).call();
        git.fetch()
                .setRemote("gitlab")
                .setCredentialsProvider(gitlabCreds)
                .setRefSpecs(new RefSpec("+refs/heads/*:refs/remotes/gitlab/*"))
                .call();
    }

    private void checkoutReviewBranch(Git git, String reviewBranch, RevCommit base) throws Exception {
        System.out.println("Service: GerritService.checkoutReviewBranch");

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
        System.out.println("Service: GerritService.parseCommit");

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
        System.out.println("Service: GerritService.computeChangeId");

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
        System.out.println("Service: GerritService.applyDiffAsPatch");

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
        System.out.println("Service: GerritService.commitWithChangeId");

        String fullMsg = originalMsg + "\n\nChange-Id: " + changeId;
        git.commit()
                .setAll(true) // commit the staged squash
                .setMessage(fullMsg)
                .call();
    }

    private PushResult pushToGerrit(Git git, CredentialsProvider gerritCreds) throws GitAPIException {
        System.out.println("Service: GerritService.pushToGerrit");

        return git.push()
                .setRemote("origin")
                .setRefSpecs(new RefSpec("HEAD:refs/for/" + gerritBranch))
                .setCredentialsProvider(gerritCreds)
                .call()
                .iterator().next();
    }

    private String extractNewSha(PushResult pr) {
        System.out.println("Service: GerritService.extractNewSha");

        return pr.getRemoteUpdates().stream()
                .map(u -> u.getNewObjectId().getName())
                .findFirst()
                .orElseThrow();
    }

}