package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Diff;
import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.service.GitLabService;

@RestController
@RequestMapping("/api/gitlab")
public class GitLabController {

    private final GitLabService gitLabService;

    @Autowired
    public GitLabController(GitLabService gitLabService) {
        this.gitLabService = gitLabService;
    }

    @GetMapping("/projects")
    public ResponseEntity<List<Project>> listProject(
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws GitLabApiException {

        String accessToken = client.getAccessToken().getTokenValue();
        return ResponseEntity.ok(gitLabService.listPersonalProject(accessToken));
    }

    @GetMapping("/get-project-commits")
    public ResponseEntity<List<Commit>> getProjectCommits(@RequestParam("projectId") String projectId,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws GitLabApiException {
        String accessToken = client.getAccessToken().getTokenValue();
        return ResponseEntity.ok(gitLabService.getProjectCommits(projectId, accessToken));
    }

    @GetMapping("/get-commit-file-diff")
    public ResponseEntity<List<Diff>> getCommitDiff(@RequestParam("projectId") String projectId,
            @RequestParam("sha") String sha,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws GitLabApiException {
        String accessToken = client.getAccessToken().getTokenValue();
        return ResponseEntity.ok(gitLabService.getCommitFileDiff(projectId, sha, accessToken));
    }

}
