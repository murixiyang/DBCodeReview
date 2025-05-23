package ic.ac.uk.db_pcr_backend.dto.gerritdto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gerrit.extensions.api.changes.ReviewInput;

public class SelectiveReviewInput extends ReviewInput {
    @JsonProperty("draft_ids_to_publish")
    public List<String> draftIdsToPublish;
}