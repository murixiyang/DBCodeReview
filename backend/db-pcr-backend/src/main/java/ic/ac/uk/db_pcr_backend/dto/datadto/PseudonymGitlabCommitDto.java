package ic.ac.uk.db_pcr_backend.dto.datadto;

import java.util.List;

import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;

public class PseudonymGitlabCommitDto {

    private GitlabCommitDto commit;
    private String authorPseudonym;

    public PseudonymGitlabCommitDto(GitlabCommitEntity commit, String authorPseudonym, List<String> redactedFields) {
        this.commit = GitlabCommitDto.fromEntity(commit, redactedFields);
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
