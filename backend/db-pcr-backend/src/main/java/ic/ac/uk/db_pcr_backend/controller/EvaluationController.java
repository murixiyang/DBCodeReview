package ic.ac.uk.db_pcr_backend.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
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

import com.google.gerrit.extensions.api.changes.ChangeApi;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.common.ChangeInput;

import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.eval.FilePayload;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;
import ic.ac.uk.db_pcr_backend.service.EvaluationService;
import ic.ac.uk.db_pcr_backend.service.GerritService;
import ic.ac.uk.db_pcr_backend.service.UserService;

@RestController
@RequestMapping("/api/eval")
public class EvaluationController {

    @Autowired
    private EvaluationService evalSvc;

    @Autowired
    private GerritService gerritSvc;

    @Autowired
    private UserService userSvc;

    /**
     * Download the ZIP template for a given project/language.
     * Returns as an octet-stream blob.
     */
    @GetMapping("/get-template")
    public ResponseEntity<Resource> getTemplateDownloaded(
            @RequestParam("language") String language) throws IOException {

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
     * Expects JSON: { "language": "...", "files": [ { "path": "...", "content":
     * "base64..." }, ... ] }
     */
    @PostMapping("/publish-to-gerrit")
    public ResponseEntity<?> publishToGerrit(
            @RequestParam("language") String language,
            @RequestBody List<FilePayload> files,
            @AuthenticationPrincipal OAuth2User oauth2User) throws Exception {

        String username = oauth2User.getAttribute("username").toString();

        // Find user
        UserEntity user = userSvc.getOrCreateUserByName(username);

        String projectPath = username + "/eval-project";

        gerritSvc.publishAuthorCodeToGerrit(user, projectPath, files, language);

        // 6. Return the Gerrit Change-Id so the front-end can reference it
        return ResponseEntity.ok(ChangeInfo.builder().changeId(changeId).build());
    }

}
