package ic.ac.uk.db_pcr_backend.entity;

import ic.ac.uk.db_pcr_backend.model.RoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.EnumType;

@Entity
@Table(name = "pseudonyms", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class PseudonymEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RoleType role;

    @Column(nullable = false, unique = true)
    private String name;

    /** When the pseudonym was created */
    public PseudonymEntity() {
    }

    public PseudonymEntity(RoleType role, String name) {
        this.role = role;
        this.name = name;
    }

    // --- Getters & Setters ---
    public Long getId() {
        return id;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}