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
    public List<ReviewAssignmentEntity> assignReviewers(String groupId, String groupProjectId,
            int reviewersPerStudent,
            String oauthToken) throws Exception {

        // Fetch all “developer” members (students)
        List<Member> students = gitlabSvc.getDevInGroup(groupId, oauthToken);

        int studentNum = students.size();
        if (reviewersPerStudent < 1 || reviewersPerStudent >= studentNum) {
            throw new IllegalArgumentException("n must be between 1 and number of students–1");
        }

        // Clear any existing assignments for this project
        reviewAssignmentRepo.deleteByGroupProjectId(groupProjectId);

        // Shuffle students for randomness
        Collections.shuffle(students);

        Project groupProject = gitlabSvc.getGroupProjectById(groupId, groupProjectId, oauthToken);
        String projectName = groupProject.getName();

        // Build assignments
        List<ReviewAssignmentEntity> assignments = new ArrayList<>();
        for (int i = 0; i < studentNum; i++) {
            Member author = students.get(i);

            // Find author's fork project id
            String authorName = author.getUsername();

            for (int k = 1; k <= reviewersPerStudent; k++) {
                Member reviewer = students.get((i + k) % studentNum);
                ReviewAssignmentEntity assignment = new ReviewAssignmentEntity(
                        groupProjectId,
                        projectName,
                        authorName,
                        reviewer.getUsername());

                assignments.add(assignment);
            }
        }

        // Store into database
        return reviewAssignmentRepo.saveAll(assignments);
    }

    /** Helper to fetch existing assignments as DTOs if you need them. */
    public List<ReviewAssignmentEntity> getAssignmentsForProject(String groupProjectId) {
        return reviewAssignmentRepo.findByGroupProjectId(groupProjectId);
    }

}
