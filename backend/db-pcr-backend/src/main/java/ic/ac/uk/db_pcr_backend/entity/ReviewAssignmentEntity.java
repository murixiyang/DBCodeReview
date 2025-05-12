package ic.ac.uk.db_pcr_backend.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "review_assignments", uniqueConstraints = @UniqueConstraint(columnNames = "assignment_uuid"))
public class ReviewAssignmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Opaque UUID used in reviewer URLs */
    @Column(name = "assignment_uuid", nullable = false, unique = true, updatable = false)
    private String assignmentUuid;

    /** The “template” project that everyone forked from */
    @Column(nullable = false, updatable = false)
    private String groupProjectId;

    /** The individual fork that the author made */
    @Column(nullable = false, updatable = false)
    private String projectName;

    @Column(nullable = false, updatable = false)
    private String authorName;

    @Column(nullable = false, updatable = false)
    private String reviewerName;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.assignmentUuid = UUID.randomUUID().toString();
        this.createdAt = Instant.now();
    }

    // Constructors
    public ReviewAssignmentEntity() {
    }

    public ReviewAssignmentEntity(
            String groupProjectId,
            String projectName,
            String authorName,
            String reviewerName) {
        this.groupProjectId = groupProjectId;
        this.projectName = projectName;
        this.authorName = authorName;
        this.reviewerName = reviewerName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getAssignmentUuid() {
        return assignmentUuid;
    }

    public String getGroupProjectId() {
        return groupProjectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // (No setters for immutable fields: all fields except id are set in constructor
    // or @PrePersist)

}
