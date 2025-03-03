package ic.ac.uk.db_pcr_backend.database;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepoRepository extends JpaRepository<UserRepoEntity, Long> {

}
