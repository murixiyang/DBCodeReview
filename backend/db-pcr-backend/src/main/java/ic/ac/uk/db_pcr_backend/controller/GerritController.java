package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.model.ChangeInfoModel;
import ic.ac.uk.db_pcr_backend.model.DiffFileModel;
import ic.ac.uk.db_pcr_backend.model.ProjectInfoModel;
import ic.ac.uk.db_pcr_backend.service.GerritService;

@RestController
@RequestMapping("/api")
public class GerritController {

    @Autowired
    private GerritService gerritService;

    @GetMapping("/get-project-list")
    public List<ProjectInfoModel> getProjectList() {
        return gerritService.getProjectList();
    }

    @GetMapping("/get-changes")
    public List<ChangeInfoModel> getChanges(@RequestParam("q") String query) {
        return gerritService.getChangesWithQuery(query);
    }

    @GetMapping("/get-modified-files")
    public List<DiffFileModel> getModifiedFiles(@RequestParam("changeId") String changeId,
            @RequestParam("revisionId") String revisionId) {

        // TODO: Currently suppose only 1 revision, with revisionId = 1

        return gerritService.getModifiedFileInChange(changeId, "1");
    }

    // @GetMapping("/get-commit-list")
    // public List<ChangeInfoModel> getCommitList() {
    // return gerritService.getOriginalCommitList();
    // }

    // @GetMapping("/get-anonymous-commit-list")
    // public List<ChangeInfoModel> getAnonymousCommitList() {
    // return gerritService.getAnonymousCommitList();
    // }

}