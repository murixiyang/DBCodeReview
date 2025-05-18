package ic.ac.uk.db_pcr_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;

public interface ReviewAssignmentRepo extends JpaRepository<ReviewAssignmentEntity, Long> {
    Optional<ReviewAssignmentEntity> findById(Long id);

    List<ReviewAssignmentEntity> findByAuthor(UserEntity author);

    List<ReviewAssignmentEntity> findByReviewer(UserEntity reviewer);

    List<ReviewAssignmentEntity> findByGroupProject(ProjectEntity groupProject);

    List<ReviewAssignmentEntity> findByAuthorAndGroupProject(UserEntity author, ProjectEntity groupProject);

    Optional<ReviewAssignmentEntity> findByAuthorAndReviewerAndGroupProject(
            UserEntity author, UserEntity reviewer, ProjectEntity groupProject);

    void deleteByGroupProject(ProjectEntity groupProject);
}
