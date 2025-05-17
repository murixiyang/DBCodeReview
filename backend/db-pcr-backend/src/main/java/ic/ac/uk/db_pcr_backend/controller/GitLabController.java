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
import ic.ac.uk.db_pcr_backend.service.CommitStatusService;
import ic.ac.uk.db_pcr_backend.service.DatabaseService;
import ic.ac.uk.db_pcr_backend.service.GitLabService;

@RestController
@RequestMapping("/api/gitlab")
public class GitLabController {

    @Autowired
    private GitLabService gitLabService;

    @Autowired
    private DatabaseService databaseSvc;

    @Autowired
    private CommitStatusService commitStatusSvc;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private GitlabCommitRepo commitRepo;

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    @Value("${gitlab.group.id}")
    private String groupId;

    @GetMapping("/get-project-commits")
    public ResponseEntity<List<GitlabCommitDto>> getProjectCommits(@RequestParam("projectId") String projectId,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client)
            throws GitLabApiException {

        System.out.println("STAGE: GitLabController.getProjectCommits");

        String accessToken = client.getAccessToken().getTokenValue();

        Long projectIdLong = Long.valueOf(projectId);

        // Sync the project’s commits into the DB
        databaseSvc.syncCommitsForProject(projectIdLong, accessToken);

        // Find project
        ProjectEntity project = projectRepo.findByGitlabProjectId(projectIdLong)
                .orElseThrow(() -> new IllegalArgumentException("Unknown project id " + projectId));

        // Find commits for the project
        List<GitlabCommitEntity> commits = commitRepo.findByProject(project);

        // Convert to DTOs
        List<GitlabCommitDto> commitDtos = commits.stream()
                .map(GitlabCommitDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(commitDtos);
    }

    /* Return commit list with mapping to change status */
    @GetMapping("/get-commits-with-status")
    public ResponseEntity<List<CommitWithStatusDto>> getProjectCommitsWithStatus(
            @RequestParam("projectId") String projectId,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client)
            throws GitLabApiException {

        System.out.println("STAGE: GitLabController.getProjectCommitsWithStatus");

        String accessToken = client.getAccessToken().getTokenValue();

        Long projectIdLong = Long.valueOf(projectId);

        // Sync the project’s commits into the DB
        databaseSvc.syncCommitsForProject(projectIdLong, accessToken);

        // Find project
        ProjectEntity project = projectRepo.findByGitlabProjectId(projectIdLong)
                .orElseThrow(() -> new IllegalArgumentException("Unknown project id " + projectId));

        // Find commits for the project
        List<GitlabCommitEntity> commits = commitRepo.findByProject(project);

        List<CommitWithStatusDto> result = commits.stream()
                .map(commit -> {
                    CommitStatus summary = commitStatusSvc.summarizeCommit(commit);
                    return new CommitWithStatusDto(GitlabCommitDto.fromEntity(commit), summary);
                })
                .toList();

        return ResponseEntity.ok(result);
    }

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
