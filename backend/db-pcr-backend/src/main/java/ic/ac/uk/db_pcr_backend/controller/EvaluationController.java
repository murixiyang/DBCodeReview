package ic.ac.uk.db_pcr_backend.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.dto.eval.EvalReviewDto;
import ic.ac.uk.db_pcr_backend.dto.eval.FilePayload;
import ic.ac.uk.db_pcr_backend.dto.eval.NamedAuthorCodeDto;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.entity.eval.AuthorCodeEntity;
import ic.ac.uk.db_pcr_backend.entity.eval.EvalReviewerEntity;
import ic.ac.uk.db_pcr_backend.repository.eval.AuthorCodeRepo;
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

}
