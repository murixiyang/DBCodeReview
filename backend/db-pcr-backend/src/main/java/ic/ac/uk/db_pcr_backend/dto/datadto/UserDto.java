package ic.ac.uk.db_pcr_backend.dto.datadto;

import java.time.Instant;

import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.redactor.Redactor;

public class UserDto {

    private Long id;
    private Long gitlabUserId;
    private String username;
    private Instant createdAt;

    // Constructor
    public UserDto() {
    }

    public UserDto(Long id, Long gitlabUserId, String username, Instant createdAt) {
        this.id = id;
        this.gitlabUserId = gitlabUserId;
        this.username = Redactor.redact(username, null);
        this.createdAt = createdAt;
    }

    public static UserDto fromEntity(UserEntity user) {
        return new UserDto(
                user.getId(),
                user.getGitlabUserId(),
                user.getUsername(),
                user.getCreatedAt());
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGitlabUserId() {
        return gitlabUserId;
    }

    public void setGitlabUserId(Long gitlabUserId) {
        this.gitlabUserId = gitlabUserId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

}
