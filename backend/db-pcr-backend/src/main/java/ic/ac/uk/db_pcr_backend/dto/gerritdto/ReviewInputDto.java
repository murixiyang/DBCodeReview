package ic.ac.uk.db_pcr_backend.dto.gerritdto;

public class ReviewInputDto {
    private String message;
    private CommentInputDto[] comments;

    public ReviewInputDto(String message, CommentInputDto[] comments) {
        this.message = message;
        this.comments = comments;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CommentInputDto[] getComments() {
        return comments;
    }

    public void setComments(CommentInputDto[] comments) {
        this.comments = comments;
    }
}
