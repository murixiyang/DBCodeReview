package ic.ac.uk.db_pcr_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.ChangeRequestEntity;
import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;

public interface ChangeRequestRepo extends JpaRepository<ChangeRequestEntity, Long> {
    Optional<ChangeRequestEntity> findByGerritChangeId(String gerritChangeId);

    List<ChangeRequestEntity> findByAssignment(ReviewAssignmentEntity assignment);

    List<ChangeRequestEntity> findByCommit(GitlabCommitEntity commit);

    Optional<ChangeRequestEntity> findByAssignmentAndCommit(
            ReviewAssignmentEntity assignment,
            GitlabCommitEntity commit);
}
