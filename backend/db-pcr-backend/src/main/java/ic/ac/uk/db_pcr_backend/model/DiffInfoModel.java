package ic.ac.uk.db_pcr_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class DiffInfoModel {
    public DiffFileMetaInfo meta_a; // not present when the file is added
    public DiffFileMetaInfo meta_b; // not present when the file is deleted
    public String change_type; // The type of change (ADDED, MODIFIED, DELETED, RENAMED COPIED, REWRITE)
    public String intraline_status; // only set when the intraline parameter was specified in the request
    public String[] diff_header; // A list of strings representing the patch set diff header
    public DiffContentModel[] content; // The content differences in the file as a list of DiffContent entities

    public static class DiffFileMetaInfo {
        public String name;
        public String content_type;
        public int lines;
    }
}