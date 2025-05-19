package ic.ac.uk.db_pcr_backend.dto.gerritdto;

import java.time.Instant;
import java.util.Optional;

import com.google.gerrit.extensions.client.Comment;
import com.google.gerrit.extensions.common.CommentInfo;

public class CommentInfoDto {
    private String id;
    private String path;
    private String side;
    private int line;
    private CommentRangeDto range;
    private String in_reply_to;
    private String message;
    private Instant updated;

    public CommentInfoDto(String id, String path, String side, int line, CommentRangeDto range,
            String in_reply_to, String message, Instant updated) {
        this.id = id;
        this.path = path;
        this.side = side;
        this.line = line;
        this.range = range;
        this.in_reply_to = in_reply_to;
        this.message = message;
        this.updated = updated;
    }

    public static CommentInfoDto fromGerritType(String filePath, CommentInfo info) {
        String path = Optional.ofNullable(info.path).orElse(filePath);
        String side = Optional.ofNullable(info.side)
                .map(Enum::toString)
                .orElse("REVISION"); // usually the default side
        int line = Optional.ofNullable(info.line).orElse(0);
        CommentRangeDto range = Optional.ofNullable(info.range)
                .map(CommentInfoDto::fromGerritRange)
                .orElseGet(CommentRangeDto::empty);
        String inReplyTo = Optional.ofNullable(info.inReplyTo).orElse("");
        String message = Optional.ofNullable(info.message).orElse("");
        Instant updated = info.updated.toInstant();

        return new CommentInfoDto(
                info.id,
                path,
                side,
                line,
                range,
                inReplyTo,
                message,
                updated);
    }

    // Helper to convert Gerrit Comment.Range to CommentRangeDto
    private static CommentRangeDto fromGerritRange(Comment.Range range) {
        if (range == null) {
            return CommentRangeDto.empty();
        }
        return new CommentRangeDto(
                range.startLine,
                range.startCharacter,
                range.endLine,
                range.endCharacter);
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

    public CommentRangeDto getRange() {
        return range;
    }

    public void setRange(CommentRangeDto range) {
        this.range = range;
    }

    public String getIn_reply_to() {
        return in_reply_to;
    }

    public void setIn_reply_to(String in_reply_to) {
        this.in_reply_to = in_reply_to;
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