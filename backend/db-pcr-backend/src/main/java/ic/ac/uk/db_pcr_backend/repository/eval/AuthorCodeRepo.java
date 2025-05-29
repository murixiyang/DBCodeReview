package ic.ac.uk.db_pcr_backend.repository.eval;

import org.springframework.data.jpa.repository.JpaRepository;

import ic.ac.uk.db_pcr_backend.entity.eval.AuthorCodeEntity;

public interface AuthorCodeRepo extends JpaRepository<AuthorCodeEntity, Long> {
    // optional custom queries
}
