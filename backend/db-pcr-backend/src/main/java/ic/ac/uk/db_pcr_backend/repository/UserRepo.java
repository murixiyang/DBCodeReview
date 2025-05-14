package ic.ac.uk.db_pcr_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.UserEntity;

public interface UserRepo extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByGitlabUserId(Long gitlabUserId);

    Optional<UserEntity> findByUsername(String username);
}
