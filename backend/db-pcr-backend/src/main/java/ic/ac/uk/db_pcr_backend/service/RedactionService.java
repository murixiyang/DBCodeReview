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
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepo;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;

@Service
public class RedactionService {

    @Autowired
    private GitLabService gitlabSvc;

    @Autowired
    private ReviewAssignmentRepo reviewAssignmentRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private UserService userSvc;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    // Blind all usernames!!
    @Cacheable("redaction-all-usernames")
    public List<String> buildAllUsernames(String currentUsername) {
        System.out.println("Service: RedactionService.buildAllUsernames");

        // Find all users except the current user
        List<String> usernames = userRepo.findAll().stream()
                .map(UserEntity::getUsername)
                .filter(username -> !username.equals(currentUsername))
                .collect(Collectors.toList());

        return usernames;
    }

    @Cacheable("redaction-usernames-or-empty")
    public List<String> buildUsernamesOrEmpty(String currentUsername, Long gitlabUserId, Long gitlabProjectId,
            String oauthToken) {
        System.out.println("Service: RedactionService.usernamesOrEmpty");

        boolean isMaintainer = gitlabSvc.hasMaintainerAccess(gitlabProjectId, gitlabUserId, oauthToken);
        if (isMaintainer) {
            return List.of(); // No redaction needed
        }

        return buildAllUsernames(currentUsername);
    }

    // Overload version with bool if is maintainer
    @Cacheable("redaction-usernames-or-empty")
    public List<String> buildUsernamesOrEmpty(String currentUsername, boolean isMaintainer) {
        System.out.println("Service: RedactionService.usernamesOrEmpty");

        if (isMaintainer) {
            return List.of(); // No redaction needed
        }

        return buildAllUsernames(currentUsername);
    }

    /**
     * Build the list of usernames to redact from
     * any text for this change, *excluding* the current user.
     */
    @Cacheable("redaction-gitlab-group-project-id")
    public List<String> buildByGitlabGroupProjectId(Long gitlabGroupProjectId, String currentUsername)
            throws IllegalArgumentException {
        System.out.println("Service: RedactionService.buildByGitlabGroupProjectId");

        // Find User
        UserEntity currentUser = userSvc.getOrExceptionUserByName(currentUsername);

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

        return usernames;

    }

    @Cacheable("redaction-gerrit-change-id")
    public List<String> buildByGerritChangeId(String gerritChangeId, String currentUsername)
            throws IllegalArgumentException {
        System.out.println("Service: RedactionService.buildByGerritChangeId");

        // Find User
        UserEntity currentUser = userSvc.getOrExceptionUserByName(currentUsername);

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

        return usernames;

    }

}
