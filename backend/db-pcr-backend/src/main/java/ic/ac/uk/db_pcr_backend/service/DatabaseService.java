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
        private ProjectRepo projectRepo;

        @Autowired
        private GitlabGroupRepo groupRepo;

        @Autowired
        private UserRepo userRepo;

        @Autowired
        private GitlabCommitRepo commitRepo;

        

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

}
