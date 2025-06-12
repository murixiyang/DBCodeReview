package ic.ac.uk.db_pcr_backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ic.ac.uk.db_pcr_backend.entity.ChangeRequestEntity;
import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.GitlabCommitRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepo;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;

@Service
public class ChangeRequestService {
    @Autowired
    private GitlabCommitRepo commitRepo;

    @Autowired
    private ReviewAssignmentRepo reviewAssignmentRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    @Autowired
    private NotificationService notificationSvc;

    @Transactional
    public void insertNewChangeRequest(Long gitlabProjectId, String targetSha, String username, String changeId) {
        System.out.println("Service: ChangeRequestService.insertNewChangeRequest");

        // Fetch the GitlabCommitEntity you just pushed
        GitlabCommitEntity commit = commitRepo
                .findByGitlabCommitId(targetSha)
                .orElseThrow(() -> new IllegalStateException("Commit must already be in our DB"));

        // Find by parent group project
        ProjectEntity project = projectRepo
                .findByGitlabProjectId(gitlabProjectId)
                .orElseThrow(() -> new IllegalStateException("Project must already be in our DB"));

        // Find all the ReviewAssignment rows for this author+project
        List<ReviewAssignmentEntity> assignments = reviewAssignmentRepo.findByAuthorAndGroupProject(
                userRepo.findByUsername(username).get(),
                project.getParentProject());

        if (assignments.isEmpty()) {
            throw new IllegalStateException("No review assignments found for author: " + username
                    + " in group project id: " + project.getParentProject().getId());
        }

        // For each assignment create a ChangeRequestEntity
        List<ChangeRequestEntity> requests = assignments.stream()
                .map(ra -> {
                    ChangeRequestEntity cr = new ChangeRequestEntity(ra, commit, changeId);
                    return cr;
                })
                .toList();

        changeRequestRepo.saveAll(requests);

        // Send notifications to all reviewers
        notificationSvc.sendNewSubmissionNotification(changeId, assignments);
    }

}
