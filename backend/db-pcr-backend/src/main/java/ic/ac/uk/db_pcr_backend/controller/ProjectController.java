package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Group;
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
import ic.ac.uk.db_pcr_backend.service.GitLabService;
import ic.ac.uk.db_pcr_backend.service.ProjectService;
import ic.ac.uk.db_pcr_backend.service.UserService;

@Controller
@RequestMapping("/api")
public class ProjectController {
    @Autowired
    private ProjectService projectSvc;

    @Autowired
    private GitLabService gitlabSvc;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private UserService userSvc;

    @Autowired
    private GitlabGroupRepo groupRepo;

    @Value("${gitlab.eval.group.id}")
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
        UserEntity user = userSvc.getOrCreateUserByGitlabId(gitlabUserId, username);

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
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client)
            throws Exception, GitLabApiException {

        System.out.println("STAGE: ProjectController.getProjectNameInGroup");

        String accessToken = client.getAccessToken().getTokenValue();

        // Fetch all groups for this user
        List<Group> groups = gitlabSvc.getGroups(accessToken);

        List<Long> groupIds = groups.stream()
                .map(Group::getId)
                .collect(Collectors.toList());

        // Return list
        List<ProjectDto> dtos = groupIds.stream()
                .map(gitlabGroupId -> {
                    // Ensure the GitlabGroup record exists
                    GitlabGroupEntity group = groupRepo.findByGitlabGroupId(gitlabGroupId)
                            .orElseGet(() -> groupRepo
                                    .save(new GitlabGroupEntity(gitlabGroupId, "Group " + gitlabGroupId)));

                    try {
                        projectSvc.syncGroupProjects(group, accessToken);
                    } catch (GitLabApiException e) {
                        throw new RuntimeException(e);
                    }

                    // Get the list of projects in the group
                    List<ProjectEntity> projects = projectRepo.findByGroupId(group.getId());

                    return projects.stream()
                            .filter(project -> project.getParentProject() == null)
                            .map(ProjectDto::fromEntity)
                            .collect(Collectors.toList());
                })
                .flatMap(List::stream)
                .collect(Collectors.toList());

        projectRepo.flush();

        // After fetching parent projects, set them for personal projects
        projectSvc.setParentForPersonalProject(accessToken);

        return ResponseEntity.ok(dtos);

    }

}
