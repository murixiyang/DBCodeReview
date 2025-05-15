package ic.ac.uk.db_pcr_backend.service;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.entity.ChangeRequestEntity;
import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.model.CommitStatus;
import ic.ac.uk.db_pcr_backend.model.ReviewStatus;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.GitlabCommitRepo;

@Service
public class CommitStatusService {

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    @Autowired
    private GitlabCommitRepo commitRepo;

    @Autowired
    private SubmissionTrackerService submissionTrackerSvc;

    public CommitStatus summarizeCommit(GitlabCommitEntity commit) {
        // A) Has the author ever submitted beyond this commit?
        Instant lastSubAt = submissionTrackerSvc.getLastSubmittedTimestamp(commit.getAuthor().getId(),
                commit.getProject().getId());

        if (lastSubAt == null) {
            return CommitStatus.NOT_SUBMITTED;
        }
        if (commit.getCommittedAt().isBefore(lastSubAt)
                && !commit.getCommittedAt().equals(lastSubAt)) {
            return CommitStatus.SUPPRESSED_SUBMITTED;
        }
        if (commit.getCommittedAt().equals(lastSubAt)) {
            return CommitStatus.WAITING_REVIEW;
        }

        // B) If it’s been submitted, look at the review requests:
        List<ReviewStatus> reviews = changeRequestRepo.findByCommit(commit)
                .stream()
                .map(ChangeRequestEntity::getStatus)
                .toList();

        if (reviews.isEmpty()) {
            return CommitStatus.WAITING_REVIEW;
        }
        if (reviews.contains(ReviewStatus.NEED_RESOLVE)
                || reviews.contains(ReviewStatus.WAITING_RESOLVE)) {
            return CommitStatus.CHANGES_REQUESTED;
        }
        if (reviews.stream().allMatch(s -> s == ReviewStatus.APPROVED)) {
            return CommitStatus.APPROVED;
        }
        return CommitStatus.IN_REVIEW;
    }

}
