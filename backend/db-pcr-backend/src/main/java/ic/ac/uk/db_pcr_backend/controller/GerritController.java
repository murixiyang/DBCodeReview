package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.model.ChangeInfoModel;
import ic.ac.uk.db_pcr_backend.model.CommentInfoModel;
import ic.ac.uk.db_pcr_backend.model.CommentInputModel;
import ic.ac.uk.db_pcr_backend.model.DiffInfoModel;
import ic.ac.uk.db_pcr_backend.model.FileInfoModel;
import ic.ac.uk.db_pcr_backend.model.ProjectInfoModel;
import ic.ac.uk.db_pcr_backend.service.GerritService;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
public class GerritController {

    @Autowired
    private GerritService gerritService;

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

    @PutMapping("/put-draft-comment")
    public ResponseEntity<CommentInfoModel> putDraftComment(
            @RequestParam("changeId") String changeId,
            @RequestParam("revisionId") String revisionId,
            @RequestBody CommentInputModel commentInput) {
        return gerritService.putDraftComment(changeId, revisionId, commentInput);
    }

}