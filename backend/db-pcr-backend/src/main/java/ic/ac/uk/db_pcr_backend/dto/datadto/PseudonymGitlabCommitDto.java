package ic.ac.uk.db_pcr_backend.dto.datadto;

import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;

public class PseudonymGitlabCommitDto {

    private GitlabCommitDto commit;
    private String authorPseudonym;

    public PseudonymGitlabCommitDto(GitlabCommitEntity commit, String authorPseudonym) {
        this.commit = GitlabCommitDto.fromEntity(commit);
        this.authorPseudonym = authorPseudonym;
    }

    public GitlabCommitDto getCommit() {
        return commit;
    }

    public void setCommit(GitlabCommitDto commit) {
        this.commit = commit;
    }

    public String getAuthorPseudonym() {
        return authorPseudonym;
    }

    public void setAuthorPseudonym(String authorPseudonym) {
        this.authorPseudonym = authorPseudonym;
    }

}
