package ic.ac.uk.db_pcr_backend.service;

import java.util.List;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ic.ac.uk.db_pcr_backend.entity.GitlabGroupEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.repository.GitlabGroupRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;

@Service
public class ProjectService {

    @Autowired
    private GitLabService gitLabSvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private GitlabGroupRepo groupRepo;

    /* ------- Project -------- */

    /* Synchronize the personal project list to database */
    @Transactional
    public void syncPersonalProjects(@AuthenticationPrincipal OAuth2User oauth2User, String accessToken)
            throws GitLabApiException {

        Long userId = Long.valueOf(oauth2User.getAttribute("id").toString());
        String username = oauth2User.getAttribute("username").toString();

        // Ensure the User record exists
        UserEntity user = userRepo.findByGitlabUserId(userId)
                .orElseGet(() -> userRepo.save(new UserEntity(userId, username, null)));

        // Call GitLab’s API for personal projects
        List<Project> forks = gitLabSvc.getPersonalProject(accessToken);

        // Check if project exists in the database
        for (var dto : forks) {
            ProjectEntity p = projectRepo.findByGitlabProjectId(dto.getId())
                    .orElseGet(() -> new ProjectEntity(dto.getId(), dto.getName(),
                            dto.getNamespace().toString()));

            p.setOwner(user);

            projectRepo.save(p);
        }
    }

    /* Synchronize the group project list to database */
    @Transactional
    public void syncGroupProjects(String groupIdStr, String oauthToken)
            throws GitLabApiException {

        Long gitlabGroupId = Long.valueOf(groupIdStr);
        // A) Ensure the GitlabGroup record exists
        GitlabGroupEntity group = groupRepo.findByGitlabGroupId(gitlabGroupId)
                .orElseGet(() -> groupRepo
                        .save(new GitlabGroupEntity(gitlabGroupId, "Group " + gitlabGroupId)));

        // B) Fetch group projects from Gitlab
        List<Project> groupProjects = gitLabSvc.getGroupProjects(groupIdStr, oauthToken);

        for (var project : groupProjects) {
            // Upsert the owner (it may be the “template” project’s creator)
            Long ownerId = project.getOwner().getId();
            UserEntity owner = userRepo.findByGitlabUserId(ownerId)
                    .orElseGet(() -> userRepo.save(
                            new UserEntity(ownerId, project.getOwner().getUsername(),
                                    null)));

            // Upsert the Project
            ProjectEntity p = projectRepo.findByGitlabProjectId(project.getId())
                    .orElseGet(() -> new ProjectEntity(project.getId(), project.getName(),
                            project.getNamespace().getFullPath()));

            p.setGroup(group);
            p.setOwner(owner);

            projectRepo.save(p);
        }

    }

}
