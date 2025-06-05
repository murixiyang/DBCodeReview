package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Diff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.dto.datadto.CommitWithStatusDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.GitlabCommitDto;
import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.model.CommitStatus;
import ic.ac.uk.db_pcr_backend.repository.GitlabCommitRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.service.CommitService;
import ic.ac.uk.db_pcr_backend.service.GerritService;
import ic.ac.uk.db_pcr_backend.service.GitLabService;
import ic.ac.uk.db_pcr_backend.service.RedactionService;

@RestController
@RequestMapping("/api")
public class CommitController {

    @Autowired
    private GitLabService gitLabSvc;

    @Autowired
    private CommitService commitSvc;

    @Autowired
    private GerritService gerritSvc;

    @Autowired
    private RedactionService redactionSvc;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private GitlabCommitRepo commitRepo;

    @GetMapping("/get-project-commits")
    public ResponseEntity<List<GitlabCommitDto>> getProjectCommits(@RequestParam("projectId") String projectId,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client,
            @AuthenticationPrincipal OAuth2User oauth2User)
            throws GitLabApiException {

        System.out.println("STAGE: GitLabController.getProjectCommits");

        String accessToken = client.getAccessToken().getTokenValue();

        Long projectIdLong = Long.valueOf(projectId);

        // Sync the project’s commits into the DB
        commitSvc.syncCommitsForProject(projectIdLong, accessToken);

        // Find project
        ProjectEntity project = projectRepo.findById(projectIdLong)
                .orElseThrow(() -> new IllegalArgumentException("Unknown project id " + projectId));

        // Find commits for the project
        List<GitlabCommitEntity> commits = commitRepo.findByProject(project);

        // Find redacted usernames for this project
        String username = oauth2User.getAttribute("username").toString();
        List<String> redactedFields = redactionSvc.buildAllUsernames(username);

        // Convert to DTOs
        List<GitlabCommitDto> commitDtos = commits.stream()
                .map(commit -> GitlabCommitDto.fromEntity(commit, redactedFields))
                .collect(Collectors.toList());

        return ResponseEntity.ok(commitDtos);
    }

    /* Return commit list with mapping to change status */
    @GetMapping("/get-commits-with-status")
    public ResponseEntity<List<CommitWithStatusDto>> getProjectCommitsWithStatus(
            @RequestParam("projectId") String projectId,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client,
            @AuthenticationPrincipal OAuth2User oauth2User)
            throws GitLabApiException {

        System.out.println("STAGE: CommitController.getProjectCommitsWithStatus");

        String accessToken = client.getAccessToken().getTokenValue();

        Long projectIdLong = Long.valueOf(projectId);

        // Sync the project’s commits into the DB
        commitSvc.syncCommitsForProject(projectIdLong, accessToken);

        // Find project
        ProjectEntity project = projectRepo.findById(projectIdLong)
                .orElseThrow(() -> new IllegalArgumentException("Unknown project id " + projectId));

        // Find commits for the project
        List<GitlabCommitEntity> commits = commitRepo.findByProject(project);

        // Find redacted usernames for this project
        String username = oauth2User.getAttribute("username").toString();
        List<String> redactedFields = redactionSvc.buildAllUsernames(username);

        List<CommitWithStatusDto> result = commits.stream()
                .map(commit -> {
                    CommitStatus summary = commitSvc.summarizeCommit(commit);
                    return new CommitWithStatusDto(GitlabCommitDto.fromEntity(commit, redactedFields), summary);
                })
                .toList();

        return ResponseEntity.ok(result);

    }

    @GetMapping("/get-commit-diff")
    public ResponseEntity<List<Diff>> getCommitDiff(@RequestParam("projectId") String projectId,
            @RequestParam("sha") String gitlabCommitId,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client)
            throws GitLabApiException {

        System.out.println("STAGE: CommitController.getCommitDiff");

        // Get Project
        ProjectEntity project = projectRepo.findById(Long.valueOf(projectId))
                .orElseThrow(() -> new IllegalArgumentException("Unknown project id " + projectId));

        String gitlabProjectId = String.valueOf(project.getGitlabProjectId());

        String accessToken = client.getAccessToken().getTokenValue();
        return ResponseEntity.ok(gitLabSvc.getCommitDiff(gitlabProjectId, gitlabCommitId, accessToken));
    }

    /** Get Gerrit change Id via commit id */
    @GetMapping("get-gerrit-change-id")
    public ResponseEntity<String> getGerritChangeId(@RequestParam("commitId") String commitId,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client)
            throws GitLabApiException {

        System.out.println("STAGE: CommitController.getGerritChangeId");

        return ResponseEntity.ok(gerritSvc.getGerritChangeIdByCommitId(Long.valueOf(commitId)));

    }

}
