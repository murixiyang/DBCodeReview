package ic.ac.uk.db_pcr_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;

public interface ProjectRepo extends JpaRepository<ProjectEntity, Long> {
    Optional<ProjectEntity> findByGitlabProjectId(Long gitlabProjectId);

    List<ProjectEntity> findByOwnerId(Long ownerId);

    List<ProjectEntity> findByGroupId(Long groupId);

}
