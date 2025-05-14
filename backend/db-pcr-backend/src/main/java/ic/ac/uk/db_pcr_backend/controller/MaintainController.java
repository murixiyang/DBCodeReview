package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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

import ic.ac.uk.db_pcr_backend.dto.datadto.ReviewAssignmentDto;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.service.GitLabService;
import ic.ac.uk.db_pcr_backend.service.MaintainService;

@RestController
@RequestMapping("/api/maintain")
@PreAuthorize("hasRole('MAINTAINER')")
public class MaintainController {

        @Autowired
        private MaintainService maintainSvc;

        @Autowired
        private GitLabService gitlabSvc;

        @Value("${gitlab.group.id}")
        private String groupId;

        @GetMapping("/get-assigned-list")
        public ResponseEntity<List<ReviewAssignmentDto>> getAssignedList(
                        @RequestParam("projectId") String projectId) throws Exception {

                List<ReviewAssignmentEntity> assignments = maintainSvc
                                .getReviewAssignmentsForProject(Long.valueOf(projectId));

                // map entity → DTO
                List<ReviewAssignmentDto> dtos = assignments.stream().map(ReviewAssignmentDto::fromEntity)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(dtos);
        }

        @PostMapping("/assign")
        public ResponseEntity<List<ReviewAssignmentDto>> assignReviewers(
                        @RequestParam("projectId") String projectId,
                        @RequestParam("reviewerNum") int reviewerNum,
                        @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {

                String accessToken = client.getAccessToken().getTokenValue();

                List<ReviewAssignmentEntity> saved = maintainSvc.assignReviewers(groupId, projectId,
                                reviewerNum, accessToken);

                // map entity → DTO
                List<ReviewAssignmentDto> dtos = saved.stream()
                                .map(a -> new ReviewAssignmentDto(
                                                a.getAssignmentUuid(),
                                                a.getGroupProjectId(),
                                                a.getProjectName(),
                                                a.getAuthorName(),
                                                a.getReviewerName()))
                                .toList();

                return ResponseEntity.ok(dtos);
        }

}
