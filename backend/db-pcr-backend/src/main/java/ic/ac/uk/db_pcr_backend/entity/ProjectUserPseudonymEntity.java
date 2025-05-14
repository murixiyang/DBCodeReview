package ic.ac.uk.db_pcr_backend.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import ic.ac.uk.db_pcr_backend.model.RoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.ForeignKey;

@Entity
@Table(name = "project_user_pseudonyms", uniqueConstraints = {
        @UniqueConstraint(name = "uk_pup_project_role_user", columnNames = { "project_id", "role", "user_id" }),
        @UniqueConstraint(name = "uk_pup_project_role_pseudonym", columnNames = { "project_id", "role",
                "pseudonym_id" })
})
public class ProjectUserPseudonymEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pup_project"))
    private ProjectEntity project;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pup_user"))
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RoleType role;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pseudonym_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pup_pseudonym"))
    private PseudonymEntity pseudonym;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private Instant assignedAt;

    public ProjectUserPseudonymEntity() {
    }

    public ProjectUserPseudonymEntity(ProjectEntity project, UserEntity user, RoleType role,
            PseudonymEntity pseudonym) {
        this.project = project;
        this.user = user;
        this.role = role;
        this.pseudonym = pseudonym;
    }

    // Getters & setters

    public Long getId() {
        return id;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public PseudonymEntity getPseudonym() {
        return pseudonym;
    }

    public void setPseudonym(PseudonymEntity pseudonym) {
        this.pseudonym = pseudonym;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }
}
