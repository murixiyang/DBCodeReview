package ic.ac.uk.db_pcr_backend.service;

import java.util.ArrayList;
import java.util.List;

import org.gitlab4j.api.GitLabApiException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ic.ac.uk.db_pcr_backend.dto.datadto.NameCommentInfoDto;
import ic.ac.uk.db_pcr_backend.dto.gerritdto.CommentInfoDto;
import ic.ac.uk.db_pcr_backend.entity.GerritCommentEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectUserPseudonymEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.CommentType;
import ic.ac.uk.db_pcr_backend.model.RoleType;
import ic.ac.uk.db_pcr_backend.repository.GerritCommentRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectUserPseudonymRepo;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepo;

@Service
public class CommentService {

    @Autowired
    private ReviewAssignmentRepo reviewAssignmentRepo;

    @Autowired
    private ProjectUserPseudonymRepo projectUserPseudonymRepo;

    @Autowired
    private GerritCommentRepo gerritCommentRepo;

    @Transactional
    public void recordDraftComment(String gerritChangeId, String assignmentId, CommentInfoDto draftInput,
            String username)
            throws GitLabApiException {
        System.out.println("Service: CommentService.syncCommentsFor");

        // Find the assignment
        Long assignmentIdLong = Long.parseLong(assignmentId);
        ReviewAssignmentEntity assignment = reviewAssignmentRepo.findById(assignmentIdLong)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Assignment not found for id " + assignmentId));

        // Determine whether the current user is the author or reviewer
        UserEntity author = assignment.getAuthor();
        UserEntity reviewer = assignment.getReviewer();

        UserEntity commentUser;
        RoleType role;
        Boolean isAuthor = false;
        CommentType commentType = CommentType.DRAFT;
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
        GerritCommentEntity commentEntity = new GerritCommentEntity(gerritChangeId, draftInput.getId(),
                commentUser, pup.getPseudonym(), commentType, isAuthor);

        // 6) Persist them in one batch
        gerritCommentRepo.save(commentEntity);
    }

    @Transactional
    public void markCommentsPublished(String gerritChangeId, List<String> draftIdsToPublish)
            throws GitLabApiException {
        System.out.println("Service: CommentService.markCommentsPublished");

        // Find all comments with the given change ID and draft IDs
        List<GerritCommentEntity> commentsToPublish = gerritCommentRepo
                .findByGerritChangeIdAndGerritCommentIdIn(gerritChangeId, draftIdsToPublish);

        // 2) flip their status
        commentsToPublish.forEach(d -> d.setCommentType(CommentType.PUBLISHED));

        // 3) write them back
        gerritCommentRepo.saveAll(commentsToPublish);
    }

    /** Given unnamed comment, return with pseudonym attached */
    @Transactional(readOnly = true)
    public List<NameCommentInfoDto> getCommentsWithPseudonym(String gerritChangeId, List<CommentInfoDto> comments)
            throws GitLabApiException {
        System.out.println("Service: CommentService.getCommentsWithPseudonym");

        List<NameCommentInfoDto> pseudonymComments = new ArrayList<>();

        comments.forEach((comment) -> {

            // Find comment in the database
            GerritCommentEntity commentEntity = gerritCommentRepo
                    .findByGerritChangeIdAndGerritCommentId(gerritChangeId, comment.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No comments found for changeId " + gerritChangeId +
                                    " and commentId " + comment.getId()));

            NameCommentInfoDto pseudonymComment = new NameCommentInfoDto(
                    commentEntity.getPseudonym().getName(),
                    commentEntity.getIsAuthor(),
                    comment);

            pseudonymComments.add(pseudonymComment);

        });

        return pseudonymComments;
    }

    /** Given unnamed comment, return with username attached */
    @Transactional(readOnly = true)
    public List<NameCommentInfoDto> getCommentsWithUsername(String gerritChangeId, List<CommentInfoDto> comments)
            throws GitLabApiException {
        System.out.println("Service: CommentService.getCommentsWithUsername");

        List<NameCommentInfoDto> usernameComments = new ArrayList<>();

        comments.forEach((comment) -> {

            // Find comment in the database
            GerritCommentEntity commentEntity = gerritCommentRepo
                    .findByGerritChangeIdAndGerritCommentId(gerritChangeId, comment.getId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No comments found for changeId " + gerritChangeId +
                                    " and commentId " + comment.getId()));

            NameCommentInfoDto usernameComment = new NameCommentInfoDto(
                    commentEntity.getCommentUser().getUsername(),
                    commentEntity.getIsAuthor(),
                    comment);

            usernameComments.add(usernameComment);

        });

        return usernameComments;
    }

}
