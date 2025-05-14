package ic.ac.uk.db_pcr_backend.dto.datadto;

import java.time.Instant;

import ic.ac.uk.db_pcr_backend.model.RoleType;

public class ProjectUserPseudonymDto {
    private Long id;
    private Long projectId;
    private Long userId;
    private RoleType role;
    private String pseudonymName;
    private Instant assignedAt;

    // Constructor
    public ProjectUserPseudonymDto() {
    }

    public ProjectUserPseudonymDto(Long id, Long projectId, Long userId, RoleType role, Long pseudonymId,
            String pseudonymName,
            Instant assignedAt) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.role = role;
        this.pseudonymName = pseudonymName;
        this.assignedAt = assignedAt;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public String getPseudonymName() {
        return pseudonymName;
    }

    public void setPseudonymName(String pseudonymName) {
        this.pseudonymName = pseudonymName;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }

}