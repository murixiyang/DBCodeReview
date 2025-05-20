package ic.ac.uk.db_pcr_backend.dto.datadto;

import ic.ac.uk.db_pcr_backend.model.CommitStatus;

public class CommitWithStatusDto {
    private GitlabCommitDto commit;
    private CommitStatus status;

    public CommitWithStatusDto(GitlabCommitDto commit, CommitStatus status) {
        this.commit = commit;
        this.status = status;
    }

    public GitlabCommitDto getCommit() {
        return commit;
    }

    public void setCommit(GitlabCommitDto commit) {
        this.commit = commit;
    }

    public CommitStatus getStatus() {
        return status;
    }

    public void setStatus(CommitStatus status) {
        this.status = status;
    }
}