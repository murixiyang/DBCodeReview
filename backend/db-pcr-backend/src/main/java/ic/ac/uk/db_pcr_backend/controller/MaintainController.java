package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.dto.datadto.ReviewAssignmentUsernameDto;
import ic.ac.uk.db_pcr_backend.entity.ReviewAssignmentEntity;
import ic.ac.uk.db_pcr_backend.service.MaintainService;

@RestController
@RequestMapping("/api/maintain")
public class MaintainController {

    @Autowired
    private MaintainService maintainSvc;

    @GetMapping("/get-assigned-list")
    public ResponseEntity<List<ReviewAssignmentUsernameDto>> getAssignedList(
            @RequestParam("groupProjectId") String groupProjectId) throws Exception {

        System.out.println("STAGE: MaintainController.getAssignedList");

        List<ReviewAssignmentEntity> assignments = maintainSvc
                .getReviewAssignmentsForProject(Long.valueOf(groupProjectId));

        List<ReviewAssignmentUsernameDto> dtos = assignments.stream().map(ra -> {

            return new ReviewAssignmentUsernameDto(ra, ra.getAuthor(), ra.getReviewer());
        }).toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/assign")
    public ResponseEntity<List<ReviewAssignmentUsernameDto>> assignReviewers(
            @RequestParam("groupProjectId") String groupProjectId,
            @RequestParam("reviewerNum") int reviewerNum,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {

        System.out.println("STAGE: MaintainController.assignReviewers");

        String accessToken = client.getAccessToken().getTokenValue();

        List<ReviewAssignmentEntity> assignments = maintainSvc.assignReviewers(groupProjectId,
                reviewerNum,
                accessToken);

        List<ReviewAssignmentUsernameDto> dtos = assignments.stream().map(ra -> {

            return new ReviewAssignmentUsernameDto(ra, ra.getAuthor(), ra.getReviewer());
        }).toList();

        return ResponseEntity.ok(dtos);
    }

}
