package ic.ac.uk.db_pcr_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;

public interface GitlabCommitRepo extends JpaRepository<GitlabCommitEntity, Long> {
    Optional<GitlabCommitEntity> findByGitlabCommitId(String gitlabCommitId);

    List<GitlabCommitEntity> findByProject(ProjectEntity project);

    List<GitlabCommitEntity> findByAuthor(UserEntity author);

}
