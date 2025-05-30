package ic.ac.uk.db_pcr_backend.dto.eval;

import ic.ac.uk.db_pcr_backend.entity.eval.EvalReviewerEntity;

public class EvalReviewDto {

    private Long assignmentId;
    private Long round1Id;
    private boolean round1Anonymous;
    private Long round2Id;
    private boolean round2Anonymous;
    private String pseudonym;

    // Constructor
    public EvalReviewDto() {
    }

    public EvalReviewDto(Long assignmentId, Long round1Id, boolean round1Anonymous,
            Long round2Id, boolean round2Anonymous, String pseudonym) {
        this.assignmentId = assignmentId;
        this.round1Id = round1Id;
        this.round1Anonymous = round1Anonymous;
        this.round2Id = round2Id;
        this.round2Anonymous = round2Anonymous;
        this.pseudonym = pseudonym;
    }

    public static EvalReviewDto from(EvalReviewerEntity entity) {

        return new EvalReviewDto(
                entity.getId(),
                entity.getRound1().getId(),
                entity.isRound1Anonymous(),
                entity.getRound2().getId(),
                entity.isRound2Anonymous(),
                entity.getPseudonym());
    }

    // Getters and Setters
    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getRound1Id() {
        return round1Id;
    }

    public void setRound1Id(Long round1Id) {
        this.round1Id = round1Id;
    }

    public boolean isRound1Anonymous() {
        return round1Anonymous;
    }

    public void setRound1Anonymous(boolean round1Anonymous) {
        this.round1Anonymous = round1Anonymous;
    }

    public Long getRound2Id() {
        return round2Id;
    }

    public void setRound2Id(Long round2Id) {
        this.round2Id = round2Id;
    }

    public boolean isRound2Anonymous() {
        return round2Anonymous;
    }

    public void setRound2Anonymous(boolean round2Anonymous) {
        this.round2Anonymous = round2Anonymous;
    }

    public String getPseudonym() {
        return pseudonym;
    }

    public void setPseudonym(String pseudonym) {
        this.pseudonym = pseudonym;
    }

}
