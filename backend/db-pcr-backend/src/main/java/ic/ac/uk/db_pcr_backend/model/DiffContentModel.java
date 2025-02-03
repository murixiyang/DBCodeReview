package ic.ac.uk.db_pcr_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiffContentModel {
    public String[] a; // Content only in the file on side A (deleted in B).
    public String[] b; // Content only in the file on side B (added in B).
    public String[] ab; // Content in the file on both sides (unchanged).
}
