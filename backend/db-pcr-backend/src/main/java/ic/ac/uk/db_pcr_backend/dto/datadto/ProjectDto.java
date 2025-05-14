package ic.ac.uk.db_pcr_backend.dto.datadto;

import java.time.Instant;

import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;

public class ProjectDto {

    private Long id;
    private Long gitlabProjectId;
    private String name;
    private String namespace;
    private Long ownerId;
    private Long groupId;
    private Long parentProjectId;
    private Instant createdAt;

    // Constructor
    public ProjectDto() {
    }

    public ProjectDto(Long id,
            Long gitlabProjectId,
            String name,
            String namespace,
            Long ownerId,
            Long groupId,
            Long parentProjectId,
            Instant createdAt) {
        this.id = id;
        this.gitlabProjectId = gitlabProjectId;
        this.name = name;
        this.namespace = namespace;
        this.ownerId = ownerId;
        this.groupId = groupId;
        this.parentProjectId = parentProjectId;
        this.createdAt = createdAt;
    }

    public static ProjectDto fromEntity(ProjectEntity project) {
        return new ProjectDto(
                project.getId(),
                project.getGitlabProjectId(),
                project.getName(),
                project.getNamespace(),
                project.getOwner() != null ? project.getOwner().getId() : null,
                project.getGroup() != null ? project.getGroup().getId() : null,
                project.getParentProject() != null ? project.getParentProject().getId() : null,
                project.getCreatedAt());
    }

    // Getters & setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Long getParentProjectId() {
        return parentProjectId;
    }

    public void setParentProjectId(Long parentProjectId) {
        this.parentProjectId = parentProjectId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
