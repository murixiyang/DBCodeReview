package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;
import java.util.Map;
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

import ic.ac.uk.db_pcr_backend.dto.datadto.ChangeRequestDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.GitlabCommitDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.ProjectDto;
import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.model.CommitStatus;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.GitlabCommitRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
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
        private ProjectRepo projectRepo;

        @Autowired
        private GitlabCommitRepo commitRepo;

        @Autowired
        private ChangeRequestRepo changeRequestRepo;

        @Value("${gitlab.group.id}")
        private String groupId;

        /* Get list of personal projects */
        @GetMapping("/projects")
        public ResponseEntity<List<ProjectDto>> getProject(
                        @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client,
                        @AuthenticationPrincipal OAuth2User oauth2User) throws GitLabApiException {

                String accessToken = client.getAccessToken().getTokenValue();
                Long gitlabUserId = Long.valueOf(oauth2User.getAttribute("id").toString());

                databaseSvc.syncPersonalProjects(oauth2User, accessToken);

                List<ProjectEntity> projects = projectRepo.findByOwnerId(gitlabUserId);

                List<ProjectDto> dtos = projects.stream()
                                .map(ProjectDto::fromEntity)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(dtos);
        }

        /** Get project name list in a group */
        @GetMapping("/group-projects")
        public ResponseEntity<List<ProjectDto>> getProjectNameInGroup(
                        @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {
                String accessToken = client.getAccessToken().getTokenValue();

                // 1) Sync the group’s projects into the DB
                databaseSvc.syncGroupProjects(groupId, accessToken);

                // 2) Get the list of projects in the group
                List<ProjectEntity> projects = projectRepo.findByGroupId(Long.valueOf(groupId));

                List<ProjectDto> dtos = projects.stream()
                                .map(ProjectDto::fromEntity)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(dtos);

        }

        @GetMapping("/get-project-commits")
        public ResponseEntity<List<GitlabCommitDto>> getProjectCommits(@RequestParam("projectId") String projectId,
                        @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client)
                        throws GitLabApiException {
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
        @GetMapping("/get-project-commits-with-status")
        public ResponseEntity<Map<GitlabCommitDto, CommitStatus>> getProjectCommitsWithStatus(
                        @RequestParam("projectId") String projectId,
                        @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client)
                        throws GitLabApiException {
                List<GitlabCommitDto> commitDtos = getProjectCommits(projectId, client).getBody();

                commitDtos.forEach(commitDto -> {
                        GitlabCommitEntity commit = commitRepo.findByGitlabCommitId(commitDto.getGitlabCommitId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                        "Unknown commit id " + commitDto.getGitlabCommitId()));



                        nullCommitDto.setChangeStatus(commit.getChangeStatus());
                });

                return ResponseEntity.ok(commitDtos);
        }

        @GetMapping("/get-commit-diff")
        public ResponseEntity<List<Diff>> getCommitDiff(@RequestParam("projectId") String projectId,
                        @RequestParam("sha") String sha,
                        @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client)
                        throws GitLabApiException {
                String accessToken = client.getAccessToken().getTokenValue();
                return ResponseEntity.ok(gitLabService.getCommitDiff(projectId, sha, accessToken));
        }

}
