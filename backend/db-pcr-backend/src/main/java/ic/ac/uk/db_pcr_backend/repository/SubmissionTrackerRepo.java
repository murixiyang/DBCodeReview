package ic.ac.uk.db_pcr_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.SubmissionTrackerEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;

public interface SubmissionTrackerRepo
        extends JpaRepository<SubmissionTrackerEntity, Long> {

    // All events in order
    List<SubmissionTrackerEntity> findByAuthorAndProjectOrderBySubmittedAtAsc(UserEntity author, ProjectEntity project);

    // The single most recent event
    Optional<SubmissionTrackerEntity> findTopByAuthorAndProjectOrderBySubmittedAtDesc(UserEntity author,
            ProjectEntity project);

    // Find by commit
    Optional<SubmissionTrackerEntity> findBySubmittedCommit(GitlabCommitEntity commit);
}
