package ic.ac.uk.db_pcr_backend.service;

import java.util.ArrayList;
import java.util.List;

import org.gitlab4j.api.GitLabApiException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ic.ac.uk.db_pcr_backend.dto.datadto.PseudonymCommentInfoDto;
import ic.ac.uk.db_pcr_backend.dto.datadto.UsernameCommentInfoDto;
import ic.ac.uk.db_pcr_backend.dto.gerritdto.CommentInfoDto;
import ic.ac.uk.db_pcr_backend.entity.ChangeRequestEntity;
import ic.ac.uk.db_pcr_backend.entity.GerritCommentEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectUserPseudonymEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.RoleType;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.GerritCommentRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectUserPseudonymRepo;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepo;

@Service
public class CommentService {

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    @Autowired
    private ReviewAssignmentRepo reviewAssignmentRepo;

    @Autowired
    private ProjectUserPseudonymRepo projectUserPseudonymRepo;

    @Autowired
    private GerritCommentRepo gerritCommentRepo;

    @Transactional
    public void recordCommentsForChangeId(String gerritChangeId, String assignmentId, List<String> commentIdList,
            String username)
            throws GitLabApiException {
        System.out.println("Service: CommentService.syncCommentsFor");

        // 1) Load the assignment
        Long assignmentIdLong = Long.parseLong(assignmentId);
        ReviewAssignmentEntity assignment = reviewAssignmentRepo.findById(assignmentIdLong)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Assignment not found for id " + assignmentId));

        // 2) Load the change request
        ChangeRequestEntity changeRequest = changeRequestRepo
                .findByAssignmentAndGerritChangeId(assignment, gerritChangeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ChangeRequest not found for assignment " + assignmentId +
                                " and changeId " + gerritChangeId));

        // 3) Determine whether the current user is the author or reviewer
        UserEntity author = assignment.getAuthor();
        UserEntity reviewer = assignment.getReviewer();

        UserEntity commentUser;
        RoleType role;
        Boolean isAuthor = false;
        if (username.equals(author.getUsername())) {
            commentUser = author;
            role = RoleType.AUTHOR;
            isAuthor = true;
        } else if (username.equals(reviewer.getUsername())) {
            commentUser = reviewer;
            role = RoleType.REVIEWER;
        } else {
            throw new IllegalArgumentException("User " + username +
                    " is neither author nor reviewer on assignment " + assignmentId);
        }

        // 4) Find their pseudonym for this project & role
        ProjectEntity project = assignment.getGroupProject();
        ProjectUserPseudonymEntity pup = projectUserPseudonymRepo
                .findByGroupProjectAndUserAndRole(project, commentUser, role)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No pseudonym for user " + username + " in project " +
                                project.getId() + " as " + role));

        // 5) Build and save one GerritCommentEntity per comment ID
        List<GerritCommentEntity> toSave = new ArrayList<>();
        for (String commentId : commentIdList) {
            GerritCommentEntity e = new GerritCommentEntity(changeRequest, gerritChangeId, commentId,
                    commentUser, pup.getPseudonym(), isAuthor);
            toSave.add(e);
        }

        // 6) Persist them in one batch
        gerritCommentRepo.saveAll(toSave);

    }

    /** Given unnamed comment, return with pseudonym attached */
    @Transactional(readOnly = true)
    public List<PseudonymCommentInfoDto> getCommentsWithPseudonym(String gerritChangeId, List<CommentInfoDto> comments)
            throws GitLabApiException {
        System.out.println("Service: CommentService.getCommentsWithPseudonym");

        List<PseudonymCommentInfoDto> pseudonymComments = new ArrayList<>();

        comments.forEach((comment) -> {

            // Find comment in the database
            GerritCommentEntity commentEntity = gerritCommentRepo
                    .findByGerritChangeIdAndGerritCommentId(gerritChangeId, comment.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No comments found for changeId " + gerritChangeId +
                                    " and commentId " + comment.getId()));

            PseudonymCommentInfoDto pseudonymComment = new PseudonymCommentInfoDto(
                    commentEntity.getPseudonym().getName(),
                    commentEntity.getIsAuthor(),
                    comment);

            pseudonymComments.add(pseudonymComment);

        });

        return pseudonymComments;
    }

    /** Given unnamed comment, return with username attached */
    @Transactional(readOnly = true)
    public List<UsernameCommentInfoDto> getCommentsWithUsername(String gerritChangeId, List<CommentInfoDto> comments)
            throws GitLabApiException {
        System.out.println("Service: CommentService.getCommentsWithUsername");

        List<UsernameCommentInfoDto> usernameComments = new ArrayList<>();

        comments.forEach((comment) -> {

            // Find comment in the database
            GerritCommentEntity commentEntity = gerritCommentRepo
                    .findByGerritChangeIdAndGerritCommentId(gerritChangeId, comment.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No comments found for changeId " + gerritChangeId +
                                    " and commentId " + comment.getId()));

            UsernameCommentInfoDto usernameComment = new UsernameCommentInfoDto(
                    commentEntity.getCommentUser().getUsername(),
                    commentEntity.getIsAuthor(),
                    comment);

            usernameComments.add(usernameComment);

        });

        return usernameComments;
    }

}
