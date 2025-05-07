package ic.ac.uk.db_pcr_backend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.service.GerritService;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
public class GerritController {

    @Autowired
    private GerritService gerritService;

    public static record ReviewRequest(String projectId, String sha) {
    }

    @PostMapping("/post-request-review")
    public ResponseEntity<Map<String, String>> requestReview(
            @RequestBody ReviewRequest req,
            @RegisteredOAuth2AuthorizedClient("gitlab") OAuth2AuthorizedClient client) throws Exception {

        // 1) Fetch the GitLab OAuth token for this user/session
        String accessToken = client.getAccessToken().getTokenValue();

        // 2) Delegate to the service
        String gerritChangeId = gerritService.submitForReview(
                req.projectId(), req.sha(), accessToken);

        // 3) Return the new Change number to the frontend
        return ResponseEntity.ok(Map.of("changeId", gerritChangeId));
    }

}