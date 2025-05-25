package ic.ac.uk.db_pcr_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.NotificationEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;

public interface NotificationRepo extends JpaRepository<NotificationEntity, Long> {

    Optional<NotificationEntity> findById(Long id);

    List<NotificationEntity> findByRecipientOrderByCreatedAtDesc(UserEntity u);

    long countByRecipientAndSeenFalse(UserEntity recipient);

}
