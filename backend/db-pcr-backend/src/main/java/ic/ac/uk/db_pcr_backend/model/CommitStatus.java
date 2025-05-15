package ic.ac.uk.db_pcr_backend.model;

public enum CommitStatus {
    NOT_SUBMITTED, // never sent for review
    SUPPRESSED_SUBMITTED, // implicitly sent as part of a later commit
    WAITING_REVIEW, // explicitly sent and now pending any reviewer
    IN_REVIEW, // at least one reviewer has picked it up
    CHANGES_REQUESTED, // at least one reviewer asked for changes
    APPROVED // all assigned reviewers approved
}