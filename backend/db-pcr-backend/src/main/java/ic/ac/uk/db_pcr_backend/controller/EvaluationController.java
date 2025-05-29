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

import ic.ac.uk.db_pcr_backend.dto.eval.FilePayload;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.entity.eval.AuthorCodeEntity;
import ic.ac.uk.db_pcr_backend.repository.eval.AuthorCodeRepo;
import ic.ac.uk.db_pcr_backend.service.GerritService;
import ic.ac.uk.db_pcr_backend.service.UserService;

@RestController
@RequestMapping("/api/eval")
public class EvaluationController {

    @Autowired
    private GerritService gerritSvc;

    @Autowired
    private UserService userSvc;

    @Autowired
    private AuthorCodeRepo authorCodeRepo;

    /**
     * Download the ZIP template for a given project/language.
     * Returns as an octet-stream blob.
     */
    @GetMapping("/get-template")
    public ResponseEntity<Resource> getTemplateDownloaded(
            @RequestParam("language") String language) throws IOException {

        System.out.println("DBLOG: EvaluationController.getTemplateDownloaded");

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
