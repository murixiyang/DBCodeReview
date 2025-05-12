package ic.ac.uk.db_pcr_backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.Project;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepository;
import jakarta.transaction.Transactional;

@Service
public class MaintainService {

    private final GitLabService gitlabSvc;

    private final ReviewAssignmentRepository reviewAssignmentRepo;

    public MaintainService(
            GitLabService gitLabService, ReviewAssignmentRepository repo) {
        this.gitlabSvc = gitLabService;
        this.reviewAssignmentRepo = repo;
    }

    @Transactional
    public List<ReviewAssignmentEntity> assignReviewers(String groupId, String projectId, String projectName,
            int reviewersPerStudent,
            String oauthToken) throws Exception {

        // Fetch all “developer” members (students)
        List<Member> students = gitlabSvc.getDevInGroup(groupId, oauthToken);

        int studentNum = students.size();
        if (reviewersPerStudent < 1 || reviewersPerStudent >= studentNum) {
            throw new IllegalArgumentException("n must be between 1 and number of students–1");
        }

        // Clear any existing assignments for this project
        reviewAssignmentRepo.deleteByProjectId(projectId);

        // Shuffle students for randomness
        Collections.shuffle(students);

        // Build assignments
        List<ReviewAssignmentEntity> assignments = new ArrayList<>();
        for (int i = 0; i < studentNum; i++) {
            Member author = students.get(i);
            for (int k = 1; k <= reviewersPerStudent; k++) {
                Member reviewer = students.get((i + k) % studentNum);
                ReviewAssignmentEntity assignment = new ReviewAssignmentEntity();
                assignment.setProjectId(projectId);
                assignment.setProjectName(projectName);
                assignment.setAuthorName(author.getUsername());
                assignment.setReviewerName(reviewer.getUsername());
                assignments.add(assignment);
            }
        }

        // Store into database
        return reviewAssignmentRepo.saveAll(assignments);
    }

    /** Helper to fetch existing assignments as DTOs if you need them. */
    public List<ReviewAssignmentEntity> getAssignmentsForProject(String projectId) {
        return reviewAssignmentRepo.findByProjectId(projectId);
    }

    /** Get projects that a user is assigned as reviewer */
    public List<Project> getProjectsToReview(String username, String groupId, String oauthToken)
            throws GitLabApiException {
        // Get project where the user is reviewer
        List<ReviewAssignmentEntity> assigns = reviewAssignmentRepo.findByReviewerName(username);

        // Get project Id
        Set<String> projectIds = assigns.stream()
                .map(ReviewAssignmentEntity::getProjectId)
                .collect(Collectors.toSet());

        System.out.println("DBLOG: Project IDs: " + projectIds);

        System.out.println("DBLOG: Group ID: " + groupId);

        // Fetch project from GitLab
        List<Project> projects = new ArrayList<>();
        for (String projectId : projectIds) {
            Project project = gitlabSvc.getGroupProjectById(groupId, projectId, oauthToken);
            projects.add(project);
        }

        return projects;
    }
}
