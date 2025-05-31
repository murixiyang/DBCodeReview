package ic.ac.uk.db_pcr_backend.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gerrit.extensions.restapi.RestApiException;

import ic.ac.uk.db_pcr_backend.dto.eval.EvalReviewDto;
import ic.ac.uk.db_pcr_backend.dto.eval.FilePayload;
import ic.ac.uk.db_pcr_backend.dto.eval.NamedAuthorCodeDto;
import ic.ac.uk.db_pcr_backend.dto.gerritdto.CommentInfoDto;
import ic.ac.uk.db_pcr_backend.dto.gerritdto.CommentInputDto;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.entity.eval.AuthorCodeEntity;
import ic.ac.uk.db_pcr_backend.entity.eval.EvalCommentEntity;
import ic.ac.uk.db_pcr_backend.entity.eval.EvalReviewerEntity;
import ic.ac.uk.db_pcr_backend.model.ReactState;
import ic.ac.uk.db_pcr_backend.repository.eval.AuthorCodeRepo;
import ic.ac.uk.db_pcr_backend.repository.eval.EvalCommentRepo;
import ic.ac.uk.db_pcr_backend.service.EvaluationService;
import ic.ac.uk.db_pcr_backend.service.GerritService;
import ic.ac.uk.db_pcr_backend.service.PseudoNameService;
import ic.ac.uk.db_pcr_backend.service.UserService;

@RestController
@RequestMapping("/api/eval")
public class EvaluationController {

    @Autowired
    private GerritService gerritSvc;

    @Autowired
    private UserService userSvc;

    @Autowired
    private EvaluationService evalSvc;

    @Autowired
    private PseudoNameService pseudoNameSvc;

    @Autowired
    private AuthorCodeRepo authorCodeRepo;

    @Autowired
    private EvalCommentRepo evalCommentRepo;

    /**
     * Download the ZIP template for a given project/language.
     * Returns as an octet-stream blob.
     */
    @GetMapping("/get-template")
    public ResponseEntity<Resource> getTemplateDownloaded(
            @RequestParam("language") String language) throws IOException {

        System.out.println("STAGE: EvaluationController.getTemplateDownloaded");

        // Map the language to a resource name
        String key = language.toLowerCase();
        if (!key.equals("java") && !key.equals("python")) {
            return ResponseEntity.badRequest().build();
        }

        // Assume templates are in src/main/resources/templates/java.zip, python.zip
        Resource file = new ClassPathResource("eval-template/" + key + "Template.zip");
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        // Prompt download
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(key + ".zip").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(file);
    }

    /**
     * Get (or create) this user’s EvalReviewAssignment,
     * including which two submissions they will review
     * and which round is anonymous.
     */
    @GetMapping("/get-eval-review-assignment")
    public ResponseEntity<EvalReviewDto> getEvalReviewAssignment(
            @AuthenticationPrincipal OAuth2User oauth2User) {
        System.out.println("STAGE: EvaluationController.getEvalReviewAssignment");

        // 1) Resolve our UserEntity
        Long gitlabId = Long.valueOf(oauth2User.getAttribute("id").toString());
        String username = oauth2User.getAttribute("username");
        UserEntity user = userSvc.getOrCreateUserByName(gitlabId, username);

        // 2) Get-or-create their assignment
        EvalReviewerEntity evalReview = evalSvc.getOrAssignEvalReviewer(user);

        // Now we only assume one assignment per user

        // 3) Convert to DTO
        EvalReviewDto dto = EvalReviewDto.from(evalReview);

        return ResponseEntity.ok(dto);
    }

    /**
     * Get Named author code entry for the given user round
     * If this round is anonymous, display name as a random pseudonym
     * Otherwise, display name as author's username
     */
    @GetMapping("/get-named-author-code")
    public ResponseEntity<NamedAuthorCodeDto> getNamedAuthorCode(
            @RequestParam("round") Integer round,
            @AuthenticationPrincipal OAuth2User oauth2User) {
        System.out.println("STAGE: EvaluationController.getNamedAuthorCode");

        // Find the author code entry for this user
        Long gitlabId = Long.valueOf(oauth2User.getAttribute("id").toString());
        String username = oauth2User.getAttribute("username");
        UserEntity user = userSvc.getOrCreateUserByName(gitlabId, username);

        EvalReviewerEntity evalReview = evalSvc.getOrAssignEvalReviewer(user);

        AuthorCodeEntity authorCode;
        boolean isAnonymous;
        switch (round) {
            case 1:
                authorCode = evalReview.getRound1();
                isAnonymous = evalReview.isRound1Anonymous();
                break;
            case 2:
                authorCode = evalReview.getRound2();
                isAnonymous = evalReview.isRound2Anonymous();
                break;
            default:
                System.out.println("ERROR: Invalid round number: " + round);
                return ResponseEntity.badRequest().build();
        }

        // 4) Determine display name
        String displayName = isAnonymous
                ? pseudoNameSvc.generateUniqueNumberName()
                : authorCode.getAuthor().getUsername();

        // 5) Build and return DTO
        NamedAuthorCodeDto dto = new NamedAuthorCodeDto(
                authorCode.getGerritChangeId(),
                authorCode.getLanguage(),
                displayName);

        return ResponseEntity.ok(dto);
    }

    /**
     * Publish user-uploaded files to Gerrit.
     * Expects JSON: { "files": [ { "path": "...", "content":
     * "base64..." }, ... ] }
     */
    @PostMapping("/publish-to-gerrit")
    public ResponseEntity<String> publishToGerrit(
            @RequestParam("language") String language,
            @RequestBody List<FilePayload> files,
            @AuthenticationPrincipal OAuth2User oauth2User) throws Exception {

        System.out.println("STAGE: EvaluationController.publishToGerrit");

        Long gitlabUserId = Long.valueOf(oauth2User.getAttribute("id").toString());
        String username = oauth2User.getAttribute("username").toString();

        // Find user
        UserEntity user = userSvc.getOrCreateUserByName(gitlabUserId, username);

        // Find existed author code entry
        List<AuthorCodeEntity> existingEntry = authorCodeRepo.findByAuthor(user);
        if (!existingEntry.isEmpty()) {
            // If an entry exists, return with the existing Change-Id
            System.out.println("DBLOG: This user already has an author code entry.");
            return ResponseEntity.ok(existingEntry.get(0).getGerritChangeId());
        }

        String projectPath = username + "/eval-project";

        String gerritChangeId = gerritSvc.publishAuthorCodeToGerrit(user, projectPath, files, language);

        // 6. Return the Gerrit Change-Id so the front-end can reference it
        return ResponseEntity.ok(gerritChangeId);
    }

    /* ---------- COMMENTING -------------- */

    // * Get unnamed gerrit change comment */
    @GetMapping("/get-gerrit-change-comments")
    public ResponseEntity<List<CommentInfoDto>> getGerritChangeComments(
            @RequestParam("gerritChangeId") String gerritChangeId, @AuthenticationPrincipal OAuth2User oauth2User)
            throws RestApiException, GitLabApiException {

        System.out.println("STAGE: EvaluationController.getGerritChangeComments");
        ;
        String username = oauth2User.getAttribute("username").toString();

        List<CommentInfoDto> comments = gerritSvc.getGerritChangeComments(gerritChangeId, username);

        return ResponseEntity.ok(comments);
    }

    /** Fetch draft comment for specific user only (should not see other's draft) */
    @GetMapping("/get-user-gerrit-change-draft-comments")
    public ResponseEntity<List<CommentInfoDto>> getGerritChangeDraftCommentsForUser(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @AuthenticationPrincipal OAuth2User oauth2User) throws RestApiException {

        System.out.println("STAGE: EvaluationController.getGerritChangeDraftCommentsForUser");

        String username = oauth2User.getAttribute("username").toString();

        List<CommentInfoDto> drafts = gerritSvc.getGerritChangeDraftComments(gerritChangeId, username);

        List<CommentInfoDto> filtered = drafts.stream()
                .filter(dto -> {
                    return evalCommentRepo
                            .findByGerritChangeIdAndGerritCommentId(gerritChangeId, dto.getId())
                            .map(entity -> entity.getCommentUser().getUsername().equals(username))
                            .orElse(false);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(filtered);
    }

    /** Post a reviewer's draft comment to gerrit and record in database */
    @PostMapping("/post-reviewer-gerrit-draft-comment")
    public ResponseEntity<CommentInfoDto> postReviewerGerritDraftComment(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestBody CommentInputDto commentInput, @AuthenticationPrincipal OAuth2User oauth2User)
            throws RestApiException, GitLabApiException {

        System.out.println("STAGE: EvaluationController.postGerritDraftComment");

        String username = oauth2User.getAttribute("username").toString();
        UserEntity commentUser = userSvc.getOrExceptionUserByName(username);

        CommentInfoDto savedDraft = gerritSvc.postGerritDraft(gerritChangeId, commentInput, username);

        EvalCommentEntity evalCommentEntity = new EvalCommentEntity(gerritChangeId, savedDraft.getId(), commentUser);

        // Save commentEntity to database
        evalCommentRepo.save(evalCommentEntity);

        return ResponseEntity.ok(savedDraft);
    }

    @PutMapping("/update-gerrit-draft-comment")
    public ResponseEntity<CommentInfoDto> updateGerritDraftComment(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestBody CommentInputDto commentInput, @AuthenticationPrincipal OAuth2User oauth2User)
            throws RestApiException {

        System.out.println("STAGE: EvaluationController.updateGerritDraftComment");

        String username = oauth2User.getAttribute("username").toString();

        return ResponseEntity.ok(gerritSvc.updateGerritDraft(
                gerritChangeId, commentInput, username));
    }

    @DeleteMapping("/delete-gerrit-draft-comment")
    public ResponseEntity<Void> deleteGerritDraftComment(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestBody CommentInputDto commentInput, @AuthenticationPrincipal OAuth2User oauth2User)
            throws RestApiException, GitLabApiException {

        System.out.println("STAGE: EvaluationController.deleteGerritDraftComment");

        gerritSvc.deleteGerritDraft(gerritChangeId, commentInput);

        EvalCommentEntity commentToDelete = evalCommentRepo
                .findByGerritChangeIdAndGerritCommentId(gerritChangeId, commentInput.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No comment found for changeId " + gerritChangeId + " and draftId " + commentInput.getId()));

        evalCommentRepo.delete(commentToDelete);

        return ResponseEntity.noContent().build();
    }

    /*
     * Publish reviewer draft comments, mark them as published in the database,
     * and update the review status to NEED_RESOLVE or APPROVED.
     */
    @Transactional
    @PostMapping("/publish-reviewer-gerrit-draft-comments")
    public ResponseEntity<Void> publishReviewerDraftComments(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestBody List<CommentInputDto> drafts) throws Exception {

        System.out.println("STAGE: EvaluationController.publishReviewerDraftComments");

        // Convert CommentInputDto to List<String> draftIds
        List<String> draftIds = drafts.stream()
                .map(CommentInputDto::getId)
                .collect(Collectors.toList());

        gerritSvc.publishDrafts(gerritChangeId, draftIds);

        return ResponseEntity.noContent().build();
    }

    @Transactional
    @PostMapping("/post-thumb-state-for-comment")
    public ResponseEntity<Void> postThumbStateForComment(
            @RequestParam("gerritChangeId") String gerritChangeId,
            @RequestParam("gerritCommentId") String gerritCommentId,
            @RequestParam("thumbState") ReactState thumbState) throws Exception {

        System.out.println("STAGE: EvaluationController.postThumbStateForComment");

        // Load comment
        EvalCommentEntity c = evalCommentRepo
                .findByGerritChangeIdAndGerritCommentId(gerritChangeId, gerritCommentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No comment found for changeId " + gerritChangeId + " and commentId " + gerritCommentId));

        // Set and save
        c.setThumbState(thumbState);
        evalCommentRepo.save(c);

        return ResponseEntity.noContent().build();
    }

}
