package ic.ac.uk.db_pcr_backend.repository.eval;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.eval.EvalCommentEntity;

public interface EvalCommentRepo extends JpaRepository<EvalCommentEntity, Long> {
    List<EvalCommentEntity> findByGerritCommentIdIn(Collection<String> gerritCommentId);

    List<EvalCommentEntity> findByGerritCommentId(String gerritCommentId);

    Optional<EvalCommentEntity> findByGerritChangeIdAndGerritCommentId(
            String gerritChangeId, String gerritCommentId);

    // find all comments on this change-ID whose Gerrit IDs are in the given set
    List<EvalCommentEntity> findByGerritChangeIdAndGerritCommentIdIn(
            String gerritChangeId,
            Collection<String> gerritCommentIds);

}
