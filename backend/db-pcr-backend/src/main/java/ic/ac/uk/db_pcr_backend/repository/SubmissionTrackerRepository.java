package ic.ac.uk.db_pcr_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.google.common.base.Optional;

public interface SubmissionTrackerRepository
    extends JpaRepository<SubmissionTrackerRepository, Long> {
  Optional<SubmissionTrackerRepository> findByUsernameAndProjectId(String username, String projectId);
}
