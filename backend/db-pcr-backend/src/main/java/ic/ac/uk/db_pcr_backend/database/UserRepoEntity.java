package ic.ac.uk.db_pcr_backend.database;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class UserRepoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The actual repository URL or link
    private String repositoryUrl;

    // Many RepositoryLinks can be linked to one UserEntity.
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // Constructors
    public UserRepoEntity() {
    }

    public UserRepoEntity(String repositoryUrl, UserEntity user) {
        this.repositoryUrl = repositoryUrl;
        this.user = user;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

}
