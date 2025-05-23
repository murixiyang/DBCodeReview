package ic.ac.uk.db_pcr_backend.dto.gerritdto;

import java.time.Instant;
import java.util.Optional;

import com.google.gerrit.extensions.common.CommentInfo;

public class CommentInfoDto {
    private String id;
    private String path;
    private String side;
    private int line;
    private String inReplyTo;
    private String message;
    private Instant updated;

    public CommentInfoDto(String id, String path, String side, int line,
            String inReplyTo, String message, Instant updated) {
        this.id = id;
        this.path = path;
        this.side = side;
        this.line = line;
        this.inReplyTo = inReplyTo;
        this.message = message;
        this.updated = updated;
    }

    public static CommentInfoDto fromGerritType(String filePath, CommentInfo info) {
        String path = Optional.ofNullable(info.path).orElse(filePath);
        String side = Optional.ofNullable(info.side)
                .map(Enum::toString)
                .orElse("REVISION"); // usually the default side
        int line = Optional.ofNullable(info.line).orElse(0);
        String inReplyTo = Optional.ofNullable(info.inReplyTo).orElse(null);
        String message = Optional.ofNullable(info.message).orElse("");
        Instant updated = info.updated.toInstant();

        return new CommentInfoDto(
                info.id,
                path,
                side,
                line,
                inReplyTo,
                message,
                updated);
    }

    // --- Getters & Setters ---
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }

}