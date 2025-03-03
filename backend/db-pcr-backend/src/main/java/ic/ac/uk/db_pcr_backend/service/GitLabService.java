package ic.ac.uk.db_pcr_backend.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import ic.ac.uk.db_pcr_backend.Constant;

@Service
public class GitLabService {

    public ResponseEntity<String> getRepositoryCommits(String repoUrl) {
        String encodedProject = getEncodedProject(repoUrl);

        try {
            URI url = new URI(Constant.GITLAB_API_BASE_URL + "/projects/" + encodedProject +
                    "/repository/commits");

            HttpHeaders headers = new HttpHeaders();
            headers.set("PRIVATE-TOKEN", Constant.GITLAB_PERSONAL_TOKEN);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            System.out.println("after send url: " + url);

            System.out.println("Response: " + response.getBody());
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }

    }

    private String getEncodedProject(String url) {
        // https://gitlab.doc.ic.ac.uk/ly1021/dbcrtestproject01 ->
        // ly1021%2Fdbcrtestproject01

        url = url.replace("https://gitlab.doc.ic.ac.uk/", "");

        return UriUtils.encode(url, StandardCharsets.UTF_8);
    }

}
