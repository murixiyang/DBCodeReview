package ic.ac.uk.db_pcr_backend.model.GerritModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentRangeModel {
    public Integer start_line;
    public Integer start_character;
    public Integer end_line;
    public Integer end_character;
}