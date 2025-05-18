package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ic.ac.uk.db_pcr_backend.dto.datadto.ProjectDto;
import ic.ac.uk.db_pcr_backend.entity.GitlabGroupEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.repository.GitlabGroupRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;
import ic.ac.uk.db_pcr_backend.service.ProjectService;

@Controller
@RequestMapping("/api")
public class ProjectController {
    @Autowired
    private ProjectService projectSvc;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private GitlabGroupRepo groupRepo;

    @Value("${gitlab.group.id}")
    private String groupId;

    /* Get list of personal projects */
    @GetMapping("/projects")
    public ResponseEntity<List<ProjectDto>> getProject(
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client,
            @AuthenticationPrincipal OAuth2User oauth2User) throws GitLabApiException {

        System.out.println("STAGE: ProjectController.getProject");

        String accessToken = client.getAccessToken().getTokenValue();

        Long gitlabUserId = Long.valueOf(oauth2User.getAttribute("id").toString());
        String username = oauth2User.getAttribute("username").toString();

        // Ensure the User record exists
        UserEntity user = userRepo.findByGitlabUserId(gitlabUserId)
                .orElseGet(() -> userRepo.save(new UserEntity(gitlabUserId, username, null)));

        // First sync group projects (so we can set the parent project)
        Long gitlabGroupId = Long.valueOf(groupId);
        GitlabGroupEntity group = groupRepo.findByGitlabGroupId(gitlabGroupId)
                .orElseGet(() -> groupRepo
                        .save(new GitlabGroupEntity(gitlabGroupId, "Group " + gitlabGroupId)));
        projectSvc.syncGroupProjects(group, accessToken);

        // Then sync personal projects
        projectSvc.syncPersonalProjects(user, accessToken);

        List<ProjectEntity> projects = projectRepo.findByOwnerId(user.getId());

        List<ProjectDto> dtos = projects.stream()
                .map(ProjectDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /** Get project name list in a group */
    @GetMapping("/group-projects")
    public ResponseEntity<List<ProjectDto>> getProjectNameInGroup(
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {

        System.out.println("STAGE: ProjectController.getProjectNameInGroup");

        String accessToken = client.getAccessToken().getTokenValue();

        Long gitlabGroupId = Long.valueOf(groupId);
        // A) Ensure the GitlabGroup record exists
        GitlabGroupEntity group = groupRepo.findByGitlabGroupId(gitlabGroupId)
                .orElseGet(() -> groupRepo
                        .save(new GitlabGroupEntity(gitlabGroupId, "Group " + gitlabGroupId)));

        // 1) Sync the group’s projects into the DB
        projectSvc.syncGroupProjects(group, accessToken);

        // 2) Get the list of projects in the group
        List<ProjectEntity> projects = projectRepo.findByGroupId(group.getId());

        List<ProjectDto> dtos = projects.stream()
                .map(ProjectDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);

    }

}
