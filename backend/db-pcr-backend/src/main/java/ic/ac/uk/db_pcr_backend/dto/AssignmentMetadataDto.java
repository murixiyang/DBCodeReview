package ic.ac.uk.db_pcr_backend.dto;

public record AssignmentMetadataDto(
    String assignmentUuid,
    String projectName,
    String authorPseudoName,
    String reviewerPseudoName) {
}
