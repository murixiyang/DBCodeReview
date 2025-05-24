package ic.ac.uk.db_pcr_backend.dto.datadto;

import ic.ac.uk.db_pcr_backend.dto.gerritdto.CommentInfoDto;

public class NameCommentInfoDto {

    // Can be either a pseudonym or a username
    private String displayName;
    private Boolean isAuthor;
    private CommentInfoDto commentInfo;

    // --- Constructors ---
    public NameCommentInfoDto(String displayName, Boolean isAuthor, CommentInfoDto commentInfo) {
        this.displayName = displayName;
        this.isAuthor = isAuthor;
        this.commentInfo = commentInfo;
    }

    // --- Getters & Setters ---
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Boolean getIsAuthor() {
        return isAuthor;
    }

    public void setIsAuthor(Boolean isAuthor) {
        this.isAuthor = isAuthor;
    }

    public CommentInfoDto getCommentInfo() {
        return commentInfo;
    }

    public void setCommentInfo(CommentInfoDto commentInfo) {
        this.commentInfo = commentInfo;
    }

}
