package ic.ac.uk.db_pcr_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "pseudo_names", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "assignment_uuid", "real_name" }),
        @UniqueConstraint(columnNames = { "assignment_uuid", "pseudo_name" })
})
public class PseudoNameEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "assignment_uuid", nullable = false, updatable = false)
    private String assignmentUuid;

    @Column(name = "real_name", nullable = false, updatable = false)
    private String realName;

    @Column(name = "pseudo_name", nullable = false, updatable = false)
    private String pseudoName;

    // Constructors
    public PseudoNameEntity() {
    }

    public PseudoNameEntity(String assignmentUuid, String realName, String pseudoName) {
        this.assignmentUuid = assignmentUuid;
        this.realName = realName;
        this.pseudoName = pseudoName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssignmentUuid() {
        return assignmentUuid;
    }

    public void setAssignmentUuid(String assignmentUuid) {
        this.assignmentUuid = assignmentUuid;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPseudoName() {
        return pseudoName;
    }

    public void setPseudoName(String pseudoName) {
        this.pseudoName = pseudoName;
    }

}
