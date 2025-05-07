package ic.ac.uk.db_pcr_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ic.ac.uk.db_pcr_backend.entity.ReviewStatusEntity;

@Repository
public interface ReviewStatusRepository extends JpaRepository<ReviewStatusEntity, Long> {
    List<ReviewStatusEntity> findByUsernameAndProjectId(String username, String projectId);

    Optional<ReviewStatusEntity> findByUsernameAndProjectIdAndCommitSha(String username, String projectId,
            String commitSha);
}
