package ic.ac.uk.db_pcr_backend.service;

import java.time.Instant;
import java.util.List;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ic.ac.uk.db_pcr_backend.entity.ChangeRequestEntity;
import ic.ac.uk.db_pcr_backend.entity.GitlabCommitEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.CommitStatus;
import ic.ac.uk.db_pcr_backend.model.ReviewStatus;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.GitlabCommitRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;

@Service
public class CommitService {

    @Autowired
    private GitLabService gitLabSvc;

    @Autowired
    private SubmissionTrackerService submissionTrackerSvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private GitlabCommitRepo commitRepo;

    /*
     * Sync the commits for a project from GitLab into the database.
     */
    @Transactional
    public void syncCommitsForProject(Long projectId, String oauthToken) throws GitLabApiException {
        System.out.println("Service: DatabaseService.syncCommitsForProject");

        ProjectEntity project = projectRepo.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found, Group ID: " + projectId));

        String projectIdStr = String.valueOf(project.getGitlabProjectId());

        // Find the user related to the project
        UserEntity author = userRepo.findById(project.getOwner().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found, User ID: " + project.getOwner().getId()));

        List<Commit> gitlabCommits = gitLabSvc.getProjectCommits(projectIdStr, oauthToken);

        for (Commit c : gitlabCommits) {

            // upsert commit
            GitlabCommitEntity entity = commitRepo.findByGitlabCommitId(c.getId())
                    .orElseGet(() -> new GitlabCommitEntity(
                            c.getId(), project, author, c.getMessage(),
                            c.getCommittedDate().toInstant()));

            entity.setMessage(c.getMessage());
            entity.setCommittedAt(c.getCommittedDate().toInstant());

            commitRepo.save(entity);
        }
    }

    /*
     * Summarize the commit status based on the gitlab commit and its reviews status
     * from all reviewers.
     */
    public CommitStatus summarizeCommit(GitlabCommitEntity commit) {
        System.out.println("Service: CommitStatusService.summarizeCommit");

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
