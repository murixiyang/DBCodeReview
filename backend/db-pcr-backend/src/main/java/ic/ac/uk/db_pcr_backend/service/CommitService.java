package ic.ac.uk.db_pcr_backend.service;

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
import ic.ac.uk.db_pcr_backend.repository.SubmissionTrackerRepo;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;

@Service
public class CommitService {

    @Autowired
    private GitLabService gitLabSvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private GitlabCommitRepo commitRepo;

    @Autowired
    private SubmissionTrackerRepo submissionTrackerRepo;

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

        UserEntity author = commit.getAuthor();
        ProjectEntity project = commit.getProject();
        String thisGitlabCommitId = commit.getGitlabCommitId();

        // Get all submitted SHAs, in time order
        List<String> submitGitlabCommitIds = submissionTrackerRepo
                .findByAuthorAndProjectOrderBySubmittedAtAsc(author, project)
                .stream()
                .map(e -> e.getLastSubmittedCommit().getGitlabCommitId())
                .toList();

        // If no commits were submitted yet, return NOT_SUBMITTED
        if (submitGitlabCommitIds.isEmpty()) {
            return CommitStatus.NOT_SUBMITTED;
        }

        // If the commit is not submitted -- Not_SUBMITTED or SUPPRESSED_SUBMITTED
        if (!submitGitlabCommitIds.contains(thisGitlabCommitId)) {
            // determine if it's older or newer than the last‐submitted
            List<GitlabCommitEntity> all = commitRepo
                    .findByProjectOrderByCommittedAtAsc(project);

            int idxThis = all.indexOf(commit);
            int idxLast = all.indexOf(
                    commitRepo.findByGitlabCommitId(submitGitlabCommitIds.get(submitGitlabCommitIds.size() - 1))
                            .orElseThrow());

            if (idxThis < idxLast) {
                return CommitStatus.SUPPRESSED_SUBMITTED;
            } else {
                return CommitStatus.NOT_SUBMITTED;
            }
        }

        // If submitted, check the change request status
        List<ReviewStatus> reviews = changeRequestRepo
                .findByCommit(commit)
                .stream()
                .map(ChangeRequestEntity::getStatus)
                .toList();

        // no one has even opened the patch yet
        if (reviews.stream().allMatch(s -> s == ReviewStatus.NOT_REVIEWED)) {
            return CommitStatus.WAITING_REVIEW;
        }
        // any reviewer asked for changes or left comments unaddressed
        if (reviews.stream().anyMatch(s -> s == ReviewStatus.NEED_RESOLVE ||
                s == ReviewStatus.WAITING_RESOLVE)) {
            return CommitStatus.CHANGES_REQUESTED;
        }
        // everyone gave +1
        if (reviews.stream().allMatch(s -> s == ReviewStatus.APPROVED)) {
            return CommitStatus.APPROVED;
        }
        // mixture of APPROVED + NOT_REVIEWED
        return CommitStatus.IN_REVIEW;
    }

}
