package ic.ac.uk.db_pcr_backend.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import ic.ac.uk.db_pcr_backend.model.ProjectStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.EnumType;
import jakarta.persistence.ForeignKey;

@Entity
@Table(name = "review_assignments", uniqueConstraints = @UniqueConstraint(columnNames = { "author_id", "reviewer_id",
        "group_project_id" }, name = "uk_review_assignment_author_reviewer_project"))

public class ReviewAssignmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // the author being reviewed
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ra_author"))
    private UserEntity author;

    // the reviewer
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ra_reviewer"))
    private UserEntity reviewer;

    // which project (fork) this assignment refers to
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ra_project"))
    private ProjectEntity groupProject;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_status", nullable = false, length = 20)
    private ProjectStatus projectStatus = ProjectStatus.NOT_STARTED;

    @Column(name = "project_status_at", nullable = false)
    private Instant projectStatusAt = Instant.now();

    // Constructors ---

    public ReviewAssignmentEntity() {
    }

    public ReviewAssignmentEntity(UserEntity author, UserEntity reviewer, ProjectEntity groupProject) {
        this.author = author;
        this.reviewer = reviewer;
        this.groupProject = groupProject;
    }

    public Long getId() {
        return id;
    }

    public UserEntity getAuthor() {
        return author;
    }

    public void setAuthor(UserEntity author) {
        this.author = author;
    }

    public UserEntity getReviewer() {
        return reviewer;
    }

    public void setReviewer(UserEntity reviewer) {
        this.reviewer = reviewer;
    }

    public ProjectEntity getGroupProject() {
        return groupProject;
    }

    public void setGroupProject(ProjectEntity groupProject) {
        this.groupProject = groupProject;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public ProjectStatus getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(ProjectStatus projectStatus) {
        this.projectStatus = projectStatus;
        this.projectStatusAt = Instant.now();
    }

    public Instant getProjectStatusAt() {
        return projectStatusAt;
    }
}
