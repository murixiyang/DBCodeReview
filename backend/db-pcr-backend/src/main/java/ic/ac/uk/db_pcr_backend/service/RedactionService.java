package ic.ac.uk.db_pcr_backend.service;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.entity.ChangeRequestEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectUserPseudonymRepo;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepo;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;

@Service
public class RedactionService {

    @Autowired
    private ReviewAssignmentRepo reviewAssignmentRepo;

    @Autowired
    private ProjectUserPseudonymRepo pupRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    /**
     * Build the list of usernames to redact from
     * any text for this change, *excluding* the current user.
     */
    @Cacheable("redaction-gitlab-group-project-id")
    public List<String> buildByGitlabGroupProjectId(Long gitlabGroupProjectId, String currentUsername)
            throws IllegalArgumentException {
        // Find User
        UserEntity currentUser = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + currentUsername));

        // Find the group project
        ProjectEntity groupProject = projectRepo
                .findByGitlabProjectId(gitlabGroupProjectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + gitlabGroupProjectId));

        // Find all ReviewAssignments for the user (as author or reviewer)
        List<ReviewAssignmentEntity> includeUserAssignment = new java.util.ArrayList<>();
        includeUserAssignment.addAll(reviewAssignmentRepo.findByAuthorAndGroupProject(currentUser, groupProject));
        includeUserAssignment.addAll(reviewAssignmentRepo.findByReviewerAndGroupProject(currentUser, groupProject));

        // Collect usernames excpet the current user
        List<String> usernames = includeUserAssignment.stream()
                .flatMap(assignment -> Stream.of(
                        assignment.getAuthor().getUsername(),
                        assignment.getReviewer().getUsername()))
                .distinct()
                .filter(username -> !username.equals(currentUsername))
                .collect(Collectors.toList());

        System.out.println("DBLOG: RedactionService.buildByGitlabGroupProjectId() - Usernames to redact: " + usernames);
        usernames.stream()
                .forEach(username -> System.out
                        .println("DBLOG: RedactionService.buildByGitlabGroupProjectId() - Redacting: " + username));

        return usernames;

    }

    @Cacheable("redaction-gerrit-change-id")
    public List<String> buildByGerritChangeId(String gerritChangeId, String currentUsername)
            throws IllegalArgumentException {
        // Find User
        UserEntity currentUser = userRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + currentUsername));

        // Find change requests as author
        List<ChangeRequestEntity> changeRequests = changeRequestRepo.findByGerritChangeId(gerritChangeId);

        // Get group project (should be the same for all change requests)
        ProjectEntity groupProject = changeRequests.stream()
                .map(ChangeRequestEntity::getAssignment)
                .map(ReviewAssignmentEntity::getGroupProject)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No change requests found for Gerrit Change ID: "
                        + gerritChangeId));

        // Find all ReviewAssignments for the author
        List<ReviewAssignmentEntity> includeUserAssignment = new java.util.ArrayList<>();
        includeUserAssignment.addAll(reviewAssignmentRepo.findByAuthorAndGroupProject(currentUser, groupProject));
        includeUserAssignment.addAll(reviewAssignmentRepo.findByReviewerAndGroupProject(currentUser, groupProject));

        // Collect usernames excpet the current user
        List<String> usernames = includeUserAssignment.stream()
                .flatMap(assignment -> Stream.of(
                        assignment.getAuthor().getUsername(),
                        assignment.getReviewer().getUsername()))
                .distinct()
                .filter(username -> !username.equals(currentUsername))
                .collect(Collectors.toList());

        System.out
                .println("DBLOG: RedactionService.buildByGerritChangeIdAsAuthor() - Usernames to redact: " + usernames);
        usernames.stream()
                .forEach(username -> System.out
                        .println("DBLOG: RedactionService.buildByGerritChangeIdAsAuthor() - Redacting: " + username));

        return usernames;

    }

}
