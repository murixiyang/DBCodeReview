package ic.ac.uk.db_pcr_backend.dto.datadto;

import ic.ac.uk.db_pcr_backend.dto.gerritdto.CommentInfoDto;

public class UsernameCommentInfoDto {

    private String username;
    private Boolean isAuthor;
    private CommentInfoDto commentInfo;

    // --- Constructors ---
    public UsernameCommentInfoDto(String username, Boolean isAuthor, CommentInfoDto commentInfo) {
        this.username = username;
        this.isAuthor = isAuthor;
        this.commentInfo = commentInfo;
    }

    // --- Getters & Setters ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
