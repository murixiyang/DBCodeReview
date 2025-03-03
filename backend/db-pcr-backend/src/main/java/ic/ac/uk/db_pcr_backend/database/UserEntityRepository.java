package ic.ac.uk.db_pcr_backend.database;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {
    // Spring Data JPA will provide all basic CRUD operations
}
