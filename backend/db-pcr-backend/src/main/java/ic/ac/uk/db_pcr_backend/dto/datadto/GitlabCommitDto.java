package ic.ac.uk.db_pcr_backend.dto.datadto;

import java.time.Instant;

import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.redactor.Redactor;

public class GitlabCommitDto {
    private Long id;
    private String gitlabCommitId;
    private Long projectId;
    private Long authorId;
    private String message;
    private Instant committedAt;
    private Instant fetchedAt;

    // Constructor
    public GitlabCommitDto() {
    }

    public GitlabCommitDto(Long id, String gitlabCommitId, Long projectId, Long authorId, String message,
            Instant committedAt,
            Instant fetchedAt) {
        this.id = id;
        this.gitlabCommitId = gitlabCommitId;
        this.projectId = projectId;
        this.authorId = authorId;
        this.message = Redactor.redact(message, null);
        this.committedAt = committedAt;
        this.fetchedAt = fetchedAt;
    }

    public static GitlabCommitDto fromEntity(GitlabCommitEntity commit) {
        return new GitlabCommitDto(
                commit.getId(),
                commit.getGitlabCommitId(),
                commit.getProject().getId(),
                commit.getAuthor().getId(),
                commit.getMessage(),
                commit.getCommittedAt(),
                commit.getFetchedAt());
    }

    // --- Getters & setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGitlabCommitId() {
        return gitlabCommitId;
    }

    public void setGitlabCommitId(String gitlabCommitId) {
        this.gitlabCommitId = gitlabCommitId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCommittedAt() {
        return committedAt;
    }

    public void setCommittedAt(Instant committedAt) {
        this.committedAt = committedAt;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}