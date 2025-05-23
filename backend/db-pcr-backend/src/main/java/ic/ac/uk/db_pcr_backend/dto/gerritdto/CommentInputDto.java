package ic.ac.uk.db_pcr_backend.dto.gerritdto;

import java.time.Instant;

import com.google.gerrit.extensions.api.changes.ReviewInput.CommentInput;
import com.google.gerrit.extensions.client.Side;

public class CommentInputDto {

    private String id; // UUID of the comment, if exists, update comment
    private String path; // file path
    private String side; // 'PARENT' or 'REVISION'
    private int line; // if 0, it's a file comment
    private String inReplyTo; // URL encoded UUID of the comment to which this comment is a reply
    private Instant updated; // updated timestamp 'yyyy-mm-dd hh:mm:ss.fffffffff'
    private String message; // If not set and an existing draft comment, the existing draft comment is
                            // deleted.

    public CommentInputDto(String id, String path, String side, int line,
            String inReplyTo, Instant updated, String message) {
        this.id = id;
        this.path = path;
        this.side = side;
        this.line = line;
        this.inReplyTo = inReplyTo;
        this.updated = updated;
        this.message = message;
    }

    public static CommentInput fromDtoToEntity(CommentInputDto commentInputDto) {
        CommentInput commentInput = new CommentInput();
        commentInput.id = commentInputDto.getId();
        commentInput.path = commentInputDto.getPath();
        commentInput.side = Side.valueOf(commentInputDto.getSide());
        commentInput.line = commentInputDto.getLine();
        commentInput.inReplyTo = commentInputDto.getInReplyTo();
        commentInput.updated = commentInputDto.getUpdated() == null ? null
                : java.sql.Timestamp.from(commentInputDto.getUpdated());
        commentInput.message = commentInputDto.getMessage();

        System.out.println("DBLOG: CommentInputDto.fromDtoToEntity Get id: " + commentInputDto.getId());

        return commentInput;
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
