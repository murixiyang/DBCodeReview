package ic.ac.uk.db_pcr_backend.dto.datadto;

import ic.ac.uk.db_pcr_backend.dto.gerritdto.CommentInfoDto;

public class PseudonymCommentInfoDto {

    private String pseudonym;
    private Boolean isAuthor;
    private CommentInfoDto commentInfo;

    // --- Constructors ---
    public PseudonymCommentInfoDto(String pseudonym, Boolean isAuthor, CommentInfoDto commentInfo) {
        this.pseudonym = pseudonym;
        this.isAuthor = isAuthor;
        this.commentInfo = commentInfo;
    }

    // --- Getters & Setters ---
    public String getPseudonym() {
        return pseudonym;
    }

    public void setPseudonym(String pseudonym) {
        this.pseudonym = pseudonym;
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
