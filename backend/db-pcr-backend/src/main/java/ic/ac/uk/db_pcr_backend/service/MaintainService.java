package ic.ac.uk.db_pcr_backend.service;

import java.util.ArrayList;
import java.util.List;

import org.gitlab4j.api.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.model.RoleType;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.repository.ReviewAssignmentRepo;

import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintainService {

    @Autowired
    private GitLabService gitlabSvc;

    @Autowired
    private PseudoNameService pseudoNameSvc;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private UserService userSvc;

    @Autowired
    private ReviewAssignmentRepo reviewAssignmentRepo;

    @Transactional
    public List<ReviewAssignmentEntity> assignReviewers(String gitlabGroupId, String projectId,
            int reviewersPerStudent,
            String oauthToken) throws Exception {

        System.out.println("Service: MaintainService.assignReviewers");

        // Fetch all “developer” members (students)
        List<Member> students = gitlabSvc.getDevInGroup(gitlabGroupId, oauthToken);

        // Check assignment number is valid
        int studentNum = students.size();
        if (reviewersPerStudent < 1 || reviewersPerStudent >= studentNum) {
            throw new IllegalArgumentException("n must be between 1 and number of students–1");
        }

        // Get the associated project and users
        ProjectEntity project = projectRepo.findById(Long.valueOf(projectId))
                .orElseThrow();
        List<UserEntity> users = students.stream()
                .map(student -> userSvc.getOrCreateUserByGitlabId(student.getId(), student.getUsername()))
                .toList();

        // Clear any existing assignments for this project
        reviewAssignmentRepo.deleteByGroupProject(project.getParentProject());

        // Create mapping of authors to reviewers
        var result = new ArrayList<ReviewAssignmentEntity>();
        int n = users.size();
        for (int i = 0; i < n; i++) {
            UserEntity author = users.get(i);
            for (int k = 1; k <= reviewersPerStudent; k++) {
                UserEntity reviewer = users.get((i + k) % n);

                // 1) Create pseudonyms for author and reviewer
                pseudoNameSvc.getOrCreatePseudoName(project, author, RoleType.AUTHOR);
                pseudoNameSvc.getOrCreatePseudoName(project, reviewer, RoleType.REVIEWER);

                // 2) build and collect the assignment
                ReviewAssignmentEntity ra = new ReviewAssignmentEntity(author, reviewer, project);
                result.add(ra);
            }
        }

        // 3) save all assignments
        return reviewAssignmentRepo.saveAll(result);
    }

    /* Get assigned list for project */
    @Transactional(readOnly = true)
    public List<ReviewAssignmentEntity> getReviewAssignmentsForProject(Long groupProjectId) {

        System.out.println("Service: MaintainService.getReviewAssignmentsForProject");

        ProjectEntity project = projectRepo
                .findById(groupProjectId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown project id: " + groupProjectId));
        return reviewAssignmentRepo.findByGroupProject(project);
    }

}
