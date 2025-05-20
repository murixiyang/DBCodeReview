package ic.ac.uk.db_pcr_backend.dto.datadto;

import java.time.Instant;

import ic.ac.uk.db_pcr_backend.entity.GitlabGroupEntity;

public class GitlabGroupDto {

    private Long id;
    private Long gitlabGroupId;
    private String name;
    private Instant createdAt;

    // Constructor
    public GitlabGroupDto() {
    }

    public GitlabGroupDto(Long id, Long gitlabGroupId, String name, Instant createdAt) {
        this.id = id;
        this.gitlabGroupId = gitlabGroupId;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static GitlabGroupDto fromEntity(GitlabGroupEntity group) {
        return new GitlabGroupDto(
                group.getId(),
                group.getGitlabGroupId(),
                group.getName(),
                group.getCreatedAt());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGitlabGroupId() {
        return gitlabGroupId;
    }

    public void setGitlabGroupId(Long gitlabGroupId) {
        this.gitlabGroupId = gitlabGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
