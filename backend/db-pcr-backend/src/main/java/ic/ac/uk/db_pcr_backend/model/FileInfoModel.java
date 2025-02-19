package ic.ac.uk.db_pcr_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileInfoModel {
    public String status;
    public int lines_inserted;
    public int size_delta;
    public int size;
}
