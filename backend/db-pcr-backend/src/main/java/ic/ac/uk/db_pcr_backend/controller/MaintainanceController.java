package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;

import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.dto.ReviewAssignmentDto;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.service.GitLabService;
import ic.ac.uk.db_pcr_backend.service.MaintainanceService;

@RestController
@RequestMapping("/api/maintain")
@PreAuthorize("hasRole('MAINTAINER')")
public class MaintainanceController {

    @Autowired
    private MaintainanceService maintainanceSvc;

    @Autowired
    private GitLabService gitlabSvc;

    @Value("${gitlab.group.id}")
    private String groupId;

    @GetMapping("/get-assigned-list")
    public ResponseEntity<List<ReviewAssignmentDto>> getAssignedList(
            @RequestParam("projectId") String projectId) throws Exception {

        List<ReviewAssignmentEntity> saved = maintainanceSvc.getAssignmentsForProject(projectId);

        // map entity → DTO
        List<ReviewAssignmentDto> dtos = saved.stream()
                .map(a -> new ReviewAssignmentDto(
                        a.getProjectId(),
                        a.getProjectName(),
                        a.getAuthorName(),
                        a.getReviewerName()))
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/projects-to-review")
    public List<Project> getProjectsToReview(
            @RequestParam("username") String username,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {

        return maintainanceSvc.getProjectsToReview(username, groupId, client.getAccessToken().getTokenValue());
    }

    @PostMapping("/assign")
    public ResponseEntity<List<ReviewAssignmentDto>> assignReviewers(
            @RequestParam("projectId") String projectId,
            @RequestParam("reviewerNum") int reviewerNum,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {

        String accessToken = client.getAccessToken().getTokenValue();

        Project project = this.gitlabSvc.getGroupProjectById(groupId, projectId, accessToken);
        String projectName = project.getName();
        List<ReviewAssignmentEntity> saved = maintainanceSvc.assignReviewers(groupId, projectId, projectName,
                reviewerNum,
                accessToken);

        // map entity → DTO
        List<ReviewAssignmentDto> dtos = saved.stream()
                .map(a -> new ReviewAssignmentDto(
                        a.getProjectId(),
                        a.getProjectName(),
                        a.getAuthorName(),
                        a.getReviewerName()))
                .toList();

        return ResponseEntity.ok(dtos);
    }

}
