package ic.ac.uk.db_pcr_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.SubmissionTrackerEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;

public interface SubmissionTrackerRepo
    extends JpaRepository<SubmissionTrackerEntity, Long> {

  Optional<SubmissionTrackerEntity> findByAuthorAndProject(UserEntity author, ProjectEntity project);
}
