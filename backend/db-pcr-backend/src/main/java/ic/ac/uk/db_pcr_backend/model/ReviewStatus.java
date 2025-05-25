package ic.ac.uk.db_pcr_backend.model;

public enum ReviewStatus {
    NOT_REVIEWED, // never looked at
    IN_REVIEW, // currently being reviewed (making draft comments)
    WAITING_RESOLVE, // comments/replies outstanding
    APPROVED, // reviewer gave a +1
}
