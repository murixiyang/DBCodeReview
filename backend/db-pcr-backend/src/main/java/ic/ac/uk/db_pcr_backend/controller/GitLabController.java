package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Diff;
import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.dto.datadto.ProjectDto;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.service.DatabaseService;
import ic.ac.uk.db_pcr_backend.service.GitLabService;

@RestController
@RequestMapping("/api/gitlab")
public class GitLabController {

    private final GitLabService gitLabService;

    @Autowired
    private DatabaseService databaseSvc;

    private final ProjectRepo projectRepo;

    @Value("${gitlab.group.id}")
    private String groupId;

    @Autowired
    public GitLabController(GitLabService gitLabService, ProjectRepo projectRepo) {
        this.gitLabService = gitLabService;
        this.projectRepo = projectRepo;
    }

    /* Get list of personal projects */
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectDto>> getProject(
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client,
            @AuthenticationPrincipal OAuth2User oauth2User) throws GitLabApiException {

        String accessToken = client.getAccessToken().getTokenValue();
        Long gitlabUserId = Long.valueOf(oauth2User.getAttribute("id").toString());

        databaseSvc.syncPersonalProjects(gitlabUserId, accessToken);

        List<ProjectEntity> projects = projectRepo.findByOwnerId(gitlabUserId);

        List<ProjectDto> dtos = projects.stream()
                .map(ProjectDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /** Get project name list in a group */
    @GetMapping("/group-projects")
    public ResponseEntity<List<Project>> getProjectNameInGroup(
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {
        String accessToken = client.getAccessToken().getTokenValue();
        return ResponseEntity.ok(gitLabService.getGroupProjects(groupId, accessToken));

    }

    @GetMapping("/get-project-commits")
    public ResponseEntity<List<Commit>> getProjectCommits(@RequestParam("projectId") String projectId,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws GitLabApiException {
        String accessToken = client.getAccessToken().getTokenValue();
        return ResponseEntity.ok(gitLabService.getProjectCommits(projectId, accessToken));
    }

    @GetMapping("/get-commit-diff")
    public ResponseEntity<List<Diff>> getCommitDiff(@RequestParam("projectId") String projectId,
            @RequestParam("sha") String sha,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws GitLabApiException {
        String accessToken = client.getAccessToken().getTokenValue();
        return ResponseEntity.ok(gitLabService.getCommitDiff(projectId, sha, accessToken));
    }

}
