package ic.ac.uk.db_pcr_backend.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.SubmissionTrackerEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.repository.SubmissionTrackerRepo;

@Service
public class SubmissionTrackerService {

    @Autowired
    private UserService userSvc;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private SubmissionTrackerRepo submissionTrackerRepo;

    /* ------ Submssion Tracker ------ */

    /**
     * Record the last submitted SHA for a user and project.
     * 
     * @param username        The username of the user
     * @param gitlabProjectId The gitlabProjectId of the project
     * @param newGerritSha    The new SHA to record
     * @return The last submitted SHA
     */
    @Transactional
    public void recordSubmission(String username, Long gitlabProjectId, SubmissionTrackerEntity previousSubmission,
            String newGerritSha,
            GitlabCommitEntity commit) {
        System.out.println("Service: SubmissionTrackerService.recordSubmission");

        // Find User
        UserEntity user = userSvc.getOrExceptionUserByName(username);

        // Find project
        ProjectEntity project = projectRepo.findByGitlabProjectId(gitlabProjectId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Project not found, Gitlab project id: " + gitlabProjectId));

        // Upsert the SubmissionTracker record
        SubmissionTrackerEntity submissionTracker = new SubmissionTrackerEntity(user, project, previousSubmission,
                newGerritSha,
                commit);

        submissionTrackerRepo.save(submissionTracker);
    }

    @Transactional(readOnly = true)
    public SubmissionTrackerEntity getPreviousSubmission(String gitlabUsername,
            Long gitlabProjectId) {
        System.out.println("Service: SubmissionTrackerService.getPreviousSubmission");

        UserEntity author = userSvc.getOrExceptionUserByName(gitlabUsername);

        ProjectEntity project = projectRepo.findByGitlabProjectId(gitlabProjectId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown project: " + gitlabProjectId));

        return submissionTrackerRepo
                .findTopByAuthorAndProjectOrderBySubmittedAtDesc(author, project)
                .orElse(null); // or throw if you prefer
    }

    @Transactional(readOnly = true)
    public Instant getLastSubmittedTimestamp(Long authorId, Long projectId) {
        System.out.println("Service: SubmissionTrackerService.getLastSubmittedTimestamp");

        UserEntity author = userSvc.getOrExceptionUserById(authorId);
        ProjectEntity project = projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown project id: " + projectId));

        return submissionTrackerRepo
                .findTopByAuthorAndProjectOrderBySubmittedAtDesc(author, project)
                .map(SubmissionTrackerEntity::getSubmittedAt)
                .orElse(null); // or throw if you prefer
    }

}
