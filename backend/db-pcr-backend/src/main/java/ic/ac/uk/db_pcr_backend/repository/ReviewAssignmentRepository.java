package ic.ac.uk.db_pcr_backend.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;

@Repository
public interface ReviewAssignmentRepository extends JpaRepository<ReviewAssignmentEntity, Long> {

    List<ReviewAssignmentEntity> findByProjectId(String projectId);

    List<ReviewAssignmentEntity> findByReviewerName(String reviewerName);

    List<ReviewAssignmentEntity> findByAuthorName(String authorName);

    void deleteByProjectId(String projectId);
}
