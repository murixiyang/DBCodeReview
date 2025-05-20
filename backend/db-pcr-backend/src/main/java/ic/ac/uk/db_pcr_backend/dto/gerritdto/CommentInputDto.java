package ic.ac.uk.db_pcr_backend.dto.gerritdto;

import java.time.Instant;

public class CommentInputDto {

    private String id; // UUID of the comment, if exists, update comment
    private String path; // file path
    private String side; // 'PARENT' or 'REVISION'
    private int line; // if 0, it's a file comment
    private CommentRangeDto range;
    private String inReplyTo; // URL encoded UUID of the comment to which this comment is a reply
    private Instant updated; // updated timestamp 'yyyy-mm-dd hh:mm:ss.fffffffff'
    private String message; // If not set and an existing draft comment, the existing draft comment is
                            // deleted.

    public CommentInputDto(String id, String path, String side, int line, CommentRangeDto range,
            String inReplyTo, Instant updated, String message) {
        this.id = id;
        this.path = path;
        this.side = side;
        this.line = line;
        this.range = range;
        this.inReplyTo = inReplyTo;
        this.updated = updated;
        this.message = message;
    }

    // Getter and Setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public CommentRangeDto getRange() {
        return range;
    }

    public void setRange(CommentRangeDto range) {
        this.range = range;
    }

    public String getInReplyTo() {
        return inReplyTo;
    }

    public void setInReplyTo(String inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
