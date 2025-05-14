package ic.ac.uk.db_pcr_backend.dto.datadto;

import ic.ac.uk.db_pcr_backend.entity.PseudonymEntity;
import ic.ac.uk.db_pcr_backend.model.RoleType;

public class PseudonymDto {
    private Long id;
    private RoleType role;
    private String pseudonym;

    public PseudonymDto() {
    }

    public PseudonymDto(PseudonymEntity p) {
        this.id = p.getId();
        this.role = p.getRole();
        this.pseudonym = p.getPseudonym();
    }

    // --- Getters & Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public String getPseudonym() {
        return pseudonym;
    }

    public void setPseudonym(String pseudonym) {
        this.pseudonym = pseudonym;
    }
}