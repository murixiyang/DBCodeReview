package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.model.GitLabModel.GitLabCommitModel;
import ic.ac.uk.db_pcr_backend.service.GitLabService;

@RestController
@RequestMapping("/api/gitlab")
public class GitLabController {

    @Autowired
    private GitLabService gitLabService;

    @GetMapping("/get-repo-commits")
    public ResponseEntity<List<GitLabCommitModel>> getRepositoryCommits(@RequestParam("url") String repoUrl) {
        return gitLabService.getRepositoryCommits(repoUrl);
    }

}
