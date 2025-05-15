package ic.ac.uk.db_pcr_backend.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.SubmissionTrackerEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.repository.SubmissionTrackerRepo;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;

public class SubmissionTrackerService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private SubmissionTrackerRepo submissionTrackerRepo;

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

    @Transactional(readOnly = true)
    public Instant getLastSubmittedTimestamp(Long authorId, Long projectId) {
        UserEntity author = userRepo.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown user id: " + authorId));
        ProjectEntity project = projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown project id: " + projectId));

        return submissionTrackerRepo
                .findByAuthorAndProject(author, project)
                .map(SubmissionTrackerEntity::getUpdatedAt)
                .orElse(null); // or throw if you prefer
    }

}
