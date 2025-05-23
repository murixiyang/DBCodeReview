package ic.ac.uk.db_pcr_backend.dto.datadto;

import ic.ac.uk.db_pcr_backend.dto.gerritdto.CommentInfoDto;

public class GerritCommentPsudoDto {
    private CommentInfoDto commentInfo;
    private String commentUserPseudonym;

    public GerritCommentPsudoDto(CommentInfoDto commentInfo, String commentUserPseudonym) {
        this.commentInfo = commentInfo;
        this.commentUserPseudonym = commentUserPseudonym;
    }

    public static GerritCommentPsudoDto fromEntity(CommentInfoDto commentInfo, String commentUserPseudonym) {
        return new GerritCommentPsudoDto(commentInfo, commentUserPseudonym);
    }


    public CommentInfoDto getCommentInfo() {
        return commentInfo;
    }

    public void setCommentInfo(CommentInfoDto commentInfo) {
        this.commentInfo = commentInfo;
    }

    public String getCommentUserPseudonym() {
        return commentUserPseudonym;
    }

    public void setCommentUserPseudonym(String commentUserPseudonym) {
        this.commentUserPseudonym = commentUserPseudonym;
    }
}
