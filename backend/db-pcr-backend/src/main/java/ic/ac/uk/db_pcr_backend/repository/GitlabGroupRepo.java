package ic.ac.uk.db_pcr_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.GitlabGroupEntity;

public interface GitlabGroupRepo extends JpaRepository<GitlabGroupEntity, Long> {
    Optional<GitlabGroupEntity> findByGitlabGroupId(Long gitlabGroupId);

}
