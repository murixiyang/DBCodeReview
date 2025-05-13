package ic.ac.uk.db_pcr_backend.dto.gerritdto;

public record ChangeDiffDto(
        String oldPath,
        String newPath,
        String diff) {
}
