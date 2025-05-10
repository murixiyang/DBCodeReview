package ic.ac.uk.db_pcr_backend.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gitlab4j.api.models.Member;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepository;

@Service
public class MaintainanceService {

    private final GitLabService gitlabSvc;

    private final ReviewAssignmentRepository reviewAssignmentRepo;

    public MaintainanceService(
            GitLabService gitLabService, ReviewAssignmentRepository repo) {
        this.gitlabSvc = gitLabService;
        this.reviewAssignmentRepo = repo;
    }

    public List<ReviewAssignmentEntity> assignReviewers(String groupId, String projectId, int reviewersPerStudent,
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
                assignment.setAuthorName(author.getName());
                assignment.setReviewerName(reviewer.getName());
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
}
