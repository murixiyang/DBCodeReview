package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.model.GerritModel.ChangeInfoModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.CommentInfoModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.CommentInputModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.DiffInfoModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.FileInfoModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.ProjectInfoModel;
import ic.ac.uk.db_pcr_backend.service.GerritService;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
public class GerritController {

    @Autowired
    private GerritService gerritService;

    public static record ReviewRequest(String projectId, String sha) {
    }

    @PostMapping("/request-review")
    public ResponseEntity<Map<String, String>> requestReview(
            @RequestBody ReviewRequest req,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {

        // 1) Fetch the GitLab OAuth token for this user/session
        String accessToken = client.getAccessToken().getTokenValue();

        // 2) Delegate to the service
        String gerritChangeId = gerritService.submitForReview(
                req.projectId(), req.sha(), accessToken);

        // 3) Return the new Change number to the frontend
        return ResponseEntity.ok(Map.of("changeId", gerritChangeId));
    }

    @GetMapping("/get-project-list")
    public Map<String, ProjectInfoModel> getProjectList() {
        return gerritService.getProjectList();
    }

    @GetMapping("/get-changes")
    public List<ChangeInfoModel> getChanges(@RequestParam("q") String query) {
        return gerritService.getChangesWithQuery(query);
    }

    @GetMapping("/get-modified-file-list")
    public Map<String, FileInfoModel> getModifiedFiles(@RequestParam("changeId") String changeId,
            @RequestParam("revisionId") String revisionId) {

        // TODO: Currently suppose only 1 revision, with revisionId = 1

        return gerritService.getModifiedFileInChange(changeId, revisionId);
    }

    @GetMapping("/get-file-diff")
    public DiffInfoModel getDiff(@RequestParam("changeId") String changeId,
            @RequestParam("revisionId") String revisionId,
            @RequestParam("filePath") String filePath) {

        // TODO: Currently suppose only 1 revision, with revisionId = 1
        return gerritService.getDiffInFile(changeId, revisionId, filePath);
    }

    @GetMapping("/get-draft-comments")
    public Map<String, CommentInfoModel[]> getDraftComments(@RequestParam("changeId") String changeId,
            @RequestParam("revisionId") String revisionId) {
        return gerritService.getAllDraftComments(changeId, revisionId);
    }

    @GetMapping("/get-comments")
    public Map<String, CommentInfoModel[]> getComments(@RequestParam("changeId") String changeId,
            @RequestParam("revisionId") String revisionId) {
        return gerritService.getAllComments(changeId, revisionId);
    }

    @PutMapping("/put-draft-comment")
    public ResponseEntity<CommentInfoModel> putDraftComment(
            @RequestParam("changeId") String changeId,
            @RequestParam("revisionId") String revisionId,
            @RequestBody CommentInputModel commentInput) {
        return gerritService.putDraftComment(changeId, revisionId, commentInput);
    }

    @PutMapping("/update-draft-comment")
    public ResponseEntity<CommentInfoModel> updateDraftComment(
            @RequestParam("changeId") String changeId,
            @RequestParam("revisionId") String revisionId,
            @RequestBody CommentInputModel commentInput) {
        return gerritService.updateDraftComment(changeId, revisionId, commentInput);
    }

    @DeleteMapping("/delete-draft-comment")
    public ResponseEntity<String> deleteDraftComment(
            @RequestParam("changeId") String changeId,
            @RequestParam("revisionId") String revisionId,
            @RequestParam("draftId") String draftId) {
        return gerritService.deleteDraftComment(changeId, revisionId, draftId);
    }

}