package ic.ac.uk.db_pcr_backend.model;

public class CommentInputModel {
    public String id;
    public String path;
    public String side;
    public int line;
    public CommentRangeModel range;
    public String in_reply_to;
    public String updated;
    public String message;
}