package ic.ac.uk.db_pcr_backend.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.GerritCommentEntity;
import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;

public interface GerritCommentRepo extends JpaRepository<GerritCommentEntity, Long> {
    List<GerritCommentEntity> findByGerritCommentIdIn(Collection<String> gerritCommentId);

    List<GerritCommentEntity> findByGerritCommentId(String gerritCommentId);

    List<GerritCommentEntity> findByPseudonym(GitlabCommitEntity commit);

    Optional<GerritCommentEntity> findByGerritChangeIdAndGerritCommentId(
            String gerritChangeId, String gerritCommentId);

    // find all comments on this change-ID whose Gerrit IDs are in the given set
    List<GerritCommentEntity> findByGerritChangeIdAndGerritCommentIdIn(
            String gerritChangeId,
            Collection<String> gerritCommentIds);

}
