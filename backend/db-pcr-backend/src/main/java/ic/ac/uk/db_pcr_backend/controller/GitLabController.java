package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Diff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.dto.datadto.CommitWithStatusDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.GitlabCommitDto;
import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.model.CommitStatus;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.GitlabCommitRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.service.CommitService;
import ic.ac.uk.db_pcr_backend.service.GitLabService;

@RestController
@RequestMapping("/api")
public class GitLabController {

    @Autowired
    private GitLabService gitLabService;

    @Autowired
    private CommitService commitStatusSvc;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private GitlabCommitRepo commitRepo;

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    @Value("${gitlab.group.id}")
    private String groupId;

    @GetMapping("/get-commit-diff")
    public ResponseEntity<List<Diff>> getCommitDiff(@RequestParam("projectId") String projectId,
            @RequestParam("sha") String sha,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client)
            throws GitLabApiException {

        System.out.println("STAGE: GitLabController.getCommitDiff");

        String accessToken = client.getAccessToken().getTokenValue();
        return ResponseEntity.ok(gitLabService.getCommitDiff(projectId, sha, accessToken));
    }

}
