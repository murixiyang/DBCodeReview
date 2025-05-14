package ic.ac.uk.db_pcr_backend.model;

public enum ChangeStatus {
    NOT_SUBMITTED,
    WAITING_REVIEW,
    NOT_REVIEWED,
    NEW_COMMENTS,
    WAITING_RESOLVE,
    APPROVED,
    REJECTED
}