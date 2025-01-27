package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.model.CommitInfo;
import ic.ac.uk.db_pcr_backend.service.GerritService;

@RestController
@RequestMapping("/api")
public class GerritController {

    @Autowired
    private GerritService gerritService;

    @GetMapping("/get-commit-list")
    public List<CommitInfo> getCommitList() {
        return gerritService.getOriginalCommitList();
    }

    @GetMapping("/get-anonymous-commit-list")
    public List<CommitInfo> getAnonymousCommitList() {
        return gerritService.getAnonymousCommitList();
    }

}