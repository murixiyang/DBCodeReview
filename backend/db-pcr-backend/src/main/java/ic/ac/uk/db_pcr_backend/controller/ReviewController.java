package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;

import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.dto.AssignmentMetadataDto;
import ic.ac.uk.db_pcr_backend.service.ReviewService;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewSvc;

    @Value("${gitlab.group.id}")
    private String groupId;

    /** Get the projects that the user needs to review */
    @GetMapping("/get-projects-to-review")
    public List<Project> getProjectsToReview(
            @RequestParam("username") String username,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {

        return reviewSvc.getProjectsToReview(username, groupId, client.getAccessToken().getTokenValue());
    }

}
