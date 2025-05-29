package ic.ac.uk.db_pcr_backend.entity.eval;

import java.time.Instant;

import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "eval_author_code")
public class AuthorCodeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Assuming you have a UserEntity in com.example.model
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity author;

    @Column(name = "gerrit_change_id", nullable = false, unique = true)
    private String gerritChangeId;

    @Column(name = "language", nullable = false, length = 50)
    private String language;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    // Constructors
    public AuthorCodeEntity() {
    }

    public AuthorCodeEntity(UserEntity author, String gerritChangeId, String language) {
        this.author = author;
        this.gerritChangeId = gerritChangeId;
        this.language = language;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getAuthor() {
        return author;
    }

    public void setAuthor(UserEntity author) {
        this.author = author;
    }

    public String getGerritChangeId() {
        return gerritChangeId;
    }

    public void setGerritChangeId(String gerritChangeId) {
        this.gerritChangeId = gerritChangeId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}