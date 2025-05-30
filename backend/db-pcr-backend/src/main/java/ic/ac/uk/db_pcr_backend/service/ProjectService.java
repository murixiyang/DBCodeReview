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

@Service
public class ProjectService {

    @Autowired
    private GitLabService gitLabSvc;

    @Autowired
    private UserService userSvc;

    @Autowired
    private ProjectRepo projectRepo;

    /* ------- Project -------- */

    /* Synchronize the personal project list to database */
    @Transactional
    public void syncPersonalProjects(UserEntity user, String accessToken)
            throws GitLabApiException {

        System.out.println("Service: ProjectService.syncPersonalProjects");

        // Call GitLab’s API for personal projects
        List<Project> projects = gitLabSvc.getPersonalProject(accessToken);

        // Check if project exists in the database
        for (var project : projects) {
            ProjectEntity p = projectRepo.findByGitlabProjectId(project.getId())
                    .orElseGet(() -> new ProjectEntity(project.getId(), project.getName(),
                            project.getNamespace().getFullPath()));

            p.setOwner(user);

            projectRepo.save(p);
        }
    }

    /* Set parent for personal project (after sync group project) */
    @Transactional
    public void setParentForPersonalProject(String accessToken) throws GitLabApiException {
        System.out.println("Service: ProjectService.setParentForPersonalProject");

        List<Project> projects = gitLabSvc.getPersonalProject(accessToken);

        for (var project : projects) {

            ProjectEntity p = projectRepo.findByGitlabProjectId(project.getId())
                    .orElseGet(() -> new ProjectEntity(project.getId(), project.getName(),
                            project.getNamespace().getFullPath()));

            if (p.getParentProject() != null && p.getGroup() != null) {
                continue;
            } else {
                // Set project parent
                if (project.getForkedFromProject() != null) {
                    Project projectParent = project.getForkedFromProject();

                    ProjectEntity parent = projectRepo.findByGitlabProjectId(projectParent.getId())
                            .orElseThrow(() -> new RuntimeException("Parent project not found"));
                    p.setParentProject(parent);
                    p.setGroup(parent.getGroup());
                }
            }

            // Save the updated project
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

            User user = gitLabSvc.getUserById(gitlabOwnerId, oauthToken);

            UserEntity owner = userSvc.getOrCreateUserByGitlabId(gitlabOwnerId, user.getUsername());

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
