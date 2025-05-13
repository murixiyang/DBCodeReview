package ic.ac.uk.db_pcr_backend.dto.gerritdto;

import java.time.Instant;

public record ChangeInfoDto(
        String changeId,
        String message,
        Instant updatedAt) {
}
