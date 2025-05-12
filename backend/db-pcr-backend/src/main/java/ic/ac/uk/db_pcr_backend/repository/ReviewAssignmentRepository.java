package ic.ac.uk.db_pcr_backend.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;

@Repository
public interface ReviewAssignmentRepository extends JpaRepository<ReviewAssignmentEntity, Long> {

    /** Lookup by the opaque UUID (used in reviewer URLs) */
    Optional<ReviewAssignmentEntity> findByAssignmentUuid(String assignmentUuid);

    /** Ensure the UUID belongs to this reviewer */
    Optional<ReviewAssignmentEntity> findByAssignmentUuidAndReviewerName(
            String assignmentUuid, String reviewerName);

    /** All assignments where this user must review someone else’s fork */
    List<ReviewAssignmentEntity> findByReviewerName(String reviewerName);

    /** All assignments where this user’s fork is awaiting review */
    List<ReviewAssignmentEntity> findByAuthorName(String authorName);

    /** All assignments belonging to a particular template project */
    List<ReviewAssignmentEntity> findByGroupProjectId(String groupProjectId);

    /** delete db for group project id */
    void deleteByGroupProjectId(String groupProjectId);
}
