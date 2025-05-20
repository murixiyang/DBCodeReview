package ic.ac.uk.db_pcr_backend.dto.gerritdto;

import com.google.gerrit.extensions.client.Comment;

public class CommentRangeDto {
    private int startLine;
    private int startCharacter;
    private int endLine;
    private int endCharacter;

    public CommentRangeDto(int startLine, int startCharacter, int endLine, int endCharacter) {
        this.startLine = startLine;
        this.startCharacter = startCharacter;
        this.endLine = endLine;
        this.endCharacter = endCharacter;
    }

    public static CommentRangeDto empty() {
        return new CommentRangeDto(0, 0, 0, 0);
    }

    public static CommentRangeDto fromGerritRange(Comment.Range range) {
        return new CommentRangeDto(
                range.startLine,
                range.startCharacter,
                range.endLine,
                range.endCharacter);
    }

    // --- Getters & Setters ---
    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getStartCharacter() {
        return startCharacter;
    }

    public void setStartCharacter(int startCharacter) {
        this.startCharacter = startCharacter;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getEndCharacter() {
        return endCharacter;
    }
}
