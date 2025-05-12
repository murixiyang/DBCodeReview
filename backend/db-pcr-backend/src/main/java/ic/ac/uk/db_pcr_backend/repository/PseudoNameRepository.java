package ic.ac.uk.db_pcr_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.PseudoNameEntity;

public interface PseudoNameRepository extends JpaRepository<PseudoNameEntity, Long> {
    Optional<PseudoNameEntity> findByAssignmentUuidAndRealName(String uuid, String realName);

    List<PseudoNameEntity> findByAssignmentUuid(String uuid);

}
