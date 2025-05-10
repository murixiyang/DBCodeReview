package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.dto.ReviewAssignmentDto;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.service.MaintainanceService;

@RestController
@RequestMapping("/api/maintain")
@PreAuthorize("hasRole('MAINTAINER')")
public class MaintainanceController {

    @Autowired
    private MaintainanceService maintainanceSvc;

    @Value("${gitlab.group.id}")
    private String groupId;

    @PostMapping("/assign")
    public ResponseEntity<List<ReviewAssignmentDto>> assignReviewers(
            @RequestParam("projectId") String projectId,
            @RequestParam("reviewerNum") int reviewerNum,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {

        String accessToken = client.getAccessToken().getTokenValue();

        List<ReviewAssignmentEntity> saved = maintainanceSvc.assignReviewers(groupId, projectId, reviewerNum,
                accessToken);

        // map entity → DTO
        List<ReviewAssignmentDto> dtos = saved.stream()
                .map(a -> new ReviewAssignmentDto(
                        a.getProjectId(),
                        a.getAuthorName(),
                        a.getReviewerName()))
                .toList();

        return ResponseEntity.ok(dtos);
    }

}
