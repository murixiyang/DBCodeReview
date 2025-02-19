package ic.ac.uk.db_pcr_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangeInfoModel {
    public String id; // '<project>\~<_number>'
    @JsonProperty("triplet_id") // Rename
    public String tripletId; // '<project>~<branch>~<Change-Id>'
    public String project;
    public String branch;
    @JsonProperty("change_id")
    public String changeId;
    public String subject; // Header line of the commit message
    public String status; // NEW, MERGED, ABANDONED
    public String created; // Timestamp of when the change was created
    public String updated; // Timestamp of when the change was last updated
    public int insertions; // Number of inserted lines
    public int deletions; // Number of deleted lines
    public AccountInfoModel owner; // Account ID of the change owner
}
