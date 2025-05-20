package ic.ac.uk.db_pcr_backend.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "projects", uniqueConstraints = @UniqueConstraint(columnNames = "gitlab_project_id"))
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gitlab_project_id", nullable = false, unique = true)
    private Long gitlabProjectId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String namespace;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "owner_user_id", foreignKey = @ForeignKey(name = "fk_project_owner"))
    private UserEntity owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "group_id", foreignKey = @ForeignKey(name = "fk_project_group"))
    private GitlabGroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "parent_project_id", foreignKey = @ForeignKey(name = "fk_project_parent"))
    private ProjectEntity parentProject;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Constructor
    public ProjectEntity() {
    }

    public ProjectEntity(Long gitlabProjectId, String name, String namespace) {
        this.gitlabProjectId = gitlabProjectId;
        this.name = name;
        this.namespace = namespace;
    }

    // Getters & setters
    public Long getId() {
        return id;
    }

    public Long getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Long gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

    public GitlabGroupEntity getGroup() {
        return group;
    }

    public void setGroup(GitlabGroupEntity group) {
        this.group = group;
    }

    public ProjectEntity getParentProject() {
        return parentProject;
    }

    public void setParentProject(ProjectEntity parentProject) {
        this.parentProject = parentProject;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
