package ic.ac.uk.db_pcr_backend.service;

import java.util.List;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ic.ac.uk.db_pcr_backend.entity.GitlabGroupEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
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

    /* ------- Project -------- */

    /* Synchronize the personal project list to database */
    @Transactional
    public void syncPersonalProjects(UserEntity user, String accessToken)
            throws GitLabApiException {

        System.out.println("Service: ProjectService.syncPersonalProjects");

        // Call GitLab’s API for personal projects
        List<Project> forks = gitLabSvc.getPersonalProject(accessToken);

        // Check if project exists in the database
        for (var dto : forks) {
            ProjectEntity p = projectRepo.findByGitlabProjectId(dto.getId())
                    .orElseGet(() -> new ProjectEntity(dto.getId(), dto.getName(),
                            dto.getNamespace().getFullPath()));

            p.setOwner(user);

            projectRepo.save(p);
        }
    }

    /* Synchronize the group project list to database */
    @Transactional
    public void syncGroupProjects(GitlabGroupEntity group, String oauthToken)
            throws GitLabApiException {

        System.out.println("Service: ProjectService.syncGroupProjects");

        // B) Fetch group projects from Gitlab
        List<Project> groupProjects = gitLabSvc.getGroupProjects(group.getGitlabGroupId().toString(), oauthToken);

        for (var project : groupProjects) {
            // Upsert the owner (it may be the “template” project’s creator)
            Long gitlabOwnerId = project.getCreatorId();

            UserEntity owner = userRepo.findByGitlabUserId(gitlabOwnerId)
                    .orElseGet(() -> {
                        try {
                            User user = gitLabSvc.getUserById(gitlabOwnerId, oauthToken);

                            return userRepo.save(new UserEntity(gitlabOwnerId, user.getUsername(),
                                    null));
                        } catch (GitLabApiException e) {
                            throw new RuntimeException("Failed to fetch user from GitLab", e);
                        }
                    });

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
