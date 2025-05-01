package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.model.GitLabModel.GitLabCommitModel;
import ic.ac.uk.db_pcr_backend.service.GitLabService;

@RestController
@RequestMapping("/api/gitlab")
public class GitLabController {

    private final GitLabService gitLabService;
    private final OAuth2AuthorizedClientService clients;

    @Autowired
    public GitLabController(GitLabService gitLabService,
            OAuth2AuthorizedClientService clients) {
        this.gitLabService = gitLabService;
        this.clients = clients;
    }

    @GetMapping("/projects")
    public ResponseEntity<List<Project>> listProjects(
            OAuth2AuthenticationToken authToken) throws GitLabApiException {

        OAuth2AuthorizedClient client = clients.loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(),
                authToken.getName());
        String accessToken = client.getAccessToken().getTokenValue();

        return ResponseEntity.ok(gitLabService.listUserProjects(accessToken));
    }

    @GetMapping("/get-repo-commits")
    public ResponseEntity<List<GitLabCommitModel>> getRepositoryCommits(@RequestParam("url") String repoUrl) {
        return gitLabService.getRepositoryCommits(repoUrl);
    }

}
