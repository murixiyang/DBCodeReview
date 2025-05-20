package ic.ac.uk.db_pcr_backend.model;

public enum ReviewStatus {
    NOT_REVIEWED, // never looked at
    WAITING_RESOLVE, // comments/replies outstanding
    NEED_RESOLVE, // reviewer asked for changes
    APPROVED, // reviewer gave a +1
    REJECTED // reviewer gave a –1
}
