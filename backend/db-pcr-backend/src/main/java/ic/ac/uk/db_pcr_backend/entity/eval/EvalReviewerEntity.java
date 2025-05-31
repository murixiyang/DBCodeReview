package ic.ac.uk.db_pcr_backend.entity.eval;

import java.time.Instant;

import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class EvalReviewerEntity {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private UserEntity reviewer;

    @ManyToOne(optional = false)
    @JoinColumn(name = "round1_author_code_id")
    private AuthorCodeEntity round1;

    @Column(nullable = false)
    private boolean round1Anonymous;

    @ManyToOne(optional = false)
    @JoinColumn(name = "round2_author_code_id")
    private AuthorCodeEntity round2;

    @Column(nullable = false)
    private boolean round2Anonymous;

    @Column(nullable = false)
    private String pseudonym;

    @Column(nullable = false)
    private boolean finished = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // Constructor
    public EvalReviewerEntity() {
    }

    public EvalReviewerEntity(UserEntity reviewer, AuthorCodeEntity round1, boolean round1Anonymous,
            AuthorCodeEntity round2, boolean round2Anonymous, String pseudonym) {
        this.reviewer = reviewer;
        this.round1 = round1;
        this.round1Anonymous = round1Anonymous;
        this.round2 = round2;
        this.round2Anonymous = round2Anonymous;
        this.pseudonym = pseudonym;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getReviewer() {
        return reviewer;
    }

    public void setReviewer(UserEntity reviewer) {
        this.reviewer = reviewer;
    }

    public AuthorCodeEntity getRound1() {
        return round1;
    }

    public void setRound1(AuthorCodeEntity round1) {
        this.round1 = round1;
    }

    public boolean isRound1Anonymous() {
        return round1Anonymous;
    }

    public void setRound1Anonymous(boolean round1Anonymous) {
        this.round1Anonymous = round1Anonymous;
    }

    public AuthorCodeEntity getRound2() {
        return round2;
    }

    public void setRound2(AuthorCodeEntity round2) {
        this.round2 = round2;
    }

    public boolean isRound2Anonymous() {
        return round2Anonymous;
    }

    public void setRound2Anonymous(boolean round2Anonymous) {
        this.round2Anonymous = round2Anonymous;
    }

    public String getPseudonym() {
        return pseudonym;
    }

    public void setPseudonym(String pseudonym) {
        this.pseudonym = pseudonym;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
