package ic.ac.uk.db_pcr_backend.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.model.ChangeInfoModel;
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
    public List<ChangeInfoModel> getChanges() {
        return gerritService.getChanges();
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