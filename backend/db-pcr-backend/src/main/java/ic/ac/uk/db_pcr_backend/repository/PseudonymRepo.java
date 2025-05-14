package ic.ac.uk.db_pcr_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.PseudonymEntity;
import ic.ac.uk.db_pcr_backend.model.RoleType;

public interface PseudonymRepo extends JpaRepository<PseudonymEntity, Long> {
    List<PseudonymEntity> findByRole(RoleType role);

    Optional<PseudonymEntity> findByName(String name);
}
