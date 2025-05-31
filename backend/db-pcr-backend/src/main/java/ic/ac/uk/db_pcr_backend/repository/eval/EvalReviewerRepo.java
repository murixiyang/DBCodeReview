package ic.ac.uk.db_pcr_backend.repository.eval;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.entity.eval.EvalReviewerEntity;

public interface EvalReviewerRepo extends JpaRepository<EvalReviewerEntity, Long> {
    // optional custom queries
    Optional<EvalReviewerEntity> findByReviewer(UserEntity reviewer);
}
