package ic.ac.uk.db_pcr_backend.service;

import java.time.Instant;
import java.util.List;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.entity.GitlabGroupEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.SubmissionTrackerEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.GitlabCommitRepo;
import ic.ac.uk.db_pcr_backend.repository.GitlabGroupRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.repository.SubmissionTrackerRepo;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;

@Service
public class DatabaseService {

        @Autowired
        private GitLabService gitLabSvc;

        @Autowired
        private ChangeRequestRepo changeRequestRepo;

        @Autowired
        private ProjectRepo projectRepo;

        @Autowired
        private GitlabGroupRepo groupRepo;

        @Autowired
        private UserRepo userRepo;

        @Autowired
        private GitlabCommitRepo commitRepo;

        @Autowired
        private SubmissionTrackerRepo submissionTrackerRepo;

        /* ------- Project -------- */

        /* Synchronize the personal project list to database */
        @Transactional
        public void syncPersonalProjects(@AuthenticationPrincipal OAuth2User oauth2User, String accessToken)
                        throws GitLabApiException {

                Long userId = Long.valueOf(oauth2User.getAttribute("id").toString());
                String username = oauth2User.getAttribute("username").toString();

                // 1) Ensure the User record exists
                UserEntity user = userRepo.findByGitlabUserId(userId)
                                .orElseGet(() -> userRepo.save(new UserEntity(userId, username, null)));

                // 2) Call GitLab’s API for personal projects
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

        /* ------- Gitlab Commit -------- */
        @Transactional
        public void syncCommitsForProject(Long gitlabProjectId, String oauthToken) throws GitLabApiException {
                ProjectEntity project = projectRepo.findByGitlabProjectId(gitlabProjectId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Project not found: " + gitlabProjectId));

                String projectIdStr = String.valueOf(gitlabProjectId);

                List<Commit> gitlabCommits = gitLabSvc.getProjectCommits(projectIdStr, oauthToken);

                for (Commit c : gitlabCommits) {
                        // upsert author
                        UserEntity author = userRepo.findByGitlabUserId(c.getAuthor().getId())
                                        .orElseGet(() -> userRepo.save(
                                                        new UserEntity(c.getAuthor().getId(), c.getAuthorName(),
                                                                        null)));

                        // upsert commit
                        GitlabCommitEntity entity = commitRepo.findByGitlabCommitId(c.getId())
                                        .orElseGet(() -> new GitlabCommitEntity(
                                                        c.getId(), project, author, c.getMessage(),
                                                        c.getCommittedDate().toInstant()));

                        entity.setMessage(c.getMessage());
                        entity.setCommittedAt(c.getCommittedDate().toInstant());

                        commitRepo.save(entity);
                }
        }

        /* ------ Submssion Tracker ------ */

        /**
         * Record the last submitted SHA for a user and project.
         * 
         * @param username     The username of the user
         * @param projectId    The ID of the project
         * @param newGerritSha The new SHA to record
         * @return The last submitted SHA
         */
        @Transactional
        public String recordSubmission(String username, Long projectId, String newGerritSha) {
                // Find User
                UserEntity user = userRepo.findByUsername(username)
                                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

                // Find project
                ProjectEntity project = projectRepo.findByGitlabProjectId(projectId)
                                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

                // Upsert the SubmissionTracker record
                var submissionTracker = submissionTrackerRepo
                                .findByAuthorAndProject(user, project)
                                .orElseGet(() -> new SubmissionTrackerEntity(user, project, newGerritSha));

                submissionTracker.setLastSubmittedSha(newGerritSha);
                submissionTracker.setUpdatedAt(Instant.now());

                return submissionTrackerRepo.save(submissionTracker).getLastSubmittedSha();
        }

        @Transactional(readOnly = true)
        public String getLastSubmittedSha(String gitlabUsername,
                        Long gitlabProjectId) {
                UserEntity author = userRepo.findByUsername(gitlabUsername)
                                .orElseThrow(() -> new IllegalArgumentException("Unknown user: " + gitlabUsername));
                ProjectEntity project = projectRepo.findByGitlabProjectId(gitlabProjectId)
                                .orElseThrow(() -> new IllegalArgumentException("Unknown project: " + gitlabProjectId));

                return submissionTrackerRepo
                                .findByAuthorAndProject(author, project)
                                .map(SubmissionTrackerEntity::getLastSubmittedSha)
                                .orElse(null); // or throw if you prefer
        }
}
