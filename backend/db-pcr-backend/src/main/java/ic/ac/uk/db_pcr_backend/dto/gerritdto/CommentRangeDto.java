package ic.ac.uk.db_pcr_backend.dto.gerritdto;

import com.google.gerrit.extensions.client.Comment;

public class CommentRangeDto {
    private int start_line;
    private int start_character;
    private int end_line;
    private int end_character;

    public CommentRangeDto(int start_line, int start_character, int end_line, int end_character) {
        this.start_line = start_line;
        this.start_character = start_character;
        this.end_line = end_line;
        this.end_character = end_character;
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
        return start_line;
    }

    public void setStartLine(int start_line) {
        this.start_line = start_line;
    }

    public int getStartCharacter() {
        return start_character;
    }

    public void setStartCharacter(int start_character) {
        this.start_character = start_character;
    }

    public int getEndLine() {
        return end_line;
    }

    public void setEndLine(int end_line) {
        this.end_line = end_line;
    }

    public int getEndCharacter() {
        return end_character;
    }

    public void setEndCharacter(int end_character) {
        this.end_character = end_character;
    }
}
