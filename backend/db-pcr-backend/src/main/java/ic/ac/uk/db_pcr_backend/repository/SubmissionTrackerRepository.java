package ic.ac.uk.db_pcr_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.SubmissionTrackerEntity;

public interface SubmissionTrackerRepository
    extends JpaRepository<SubmissionTrackerEntity, Long> {
  Optional<SubmissionTrackerEntity> findByUsernameAndProjectId(String username, String projectId);
}
