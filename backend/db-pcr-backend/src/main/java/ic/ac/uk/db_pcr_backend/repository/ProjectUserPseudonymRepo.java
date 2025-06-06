package ic.ac.uk.db_pcr_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectUserPseudonymEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.RoleType;

public interface ProjectUserPseudonymRepo extends JpaRepository<ProjectUserPseudonymEntity, Long> {

    List<ProjectUserPseudonymEntity> findByGroupProjectAndRole(ProjectEntity groupProject, RoleType role);

    Optional<ProjectUserPseudonymEntity> findByGroupProjectAndUserAndRole(ProjectEntity groupProject, UserEntity user,
            RoleType role);

}
