package ic.ac.uk.db_pcr_backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import ic.ac.uk.db_pcr_backend.Constant;
import ic.ac.uk.db_pcr_backend.model.CommentInputModel;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private RestTemplate restTemplate;

    @PutMapping("/post-draft-comment")
    public ResponseEntity<?> postDraftComment(
            @RequestParam("changeId") String changeId,
            @RequestParam("revisionId") String revisionId,
            @RequestBody CommentInputModel commentInput) {

        // Construct Gerrit's API endpoint URL
        String gerritUrl = Constant.GERRIT_BASE_URL + "/changes/" + changeId +
                "/revisions/" + revisionId + "/drafts";

        // Optionally, set up HTTP headers (e.g., for authentication)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // e.g., headers.setBasicAuth("username", "http_password"); // if needed

        HttpEntity<CommentInputModel> requestEntity = new HttpEntity<>(commentInput, headers);

        // Send a PUT request to Gerrit's API
        ResponseEntity<CommentInputModel> response = restTemplate.exchange(
                gerritUrl,
                HttpMethod.PUT,
                requestEntity,
                CommentInputModel.class);

        System.out.println("Gerrit's response: " + response);

        // Return Gerrit's response back to the client
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

}
