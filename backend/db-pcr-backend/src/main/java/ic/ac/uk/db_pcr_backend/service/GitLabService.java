package ic.ac.uk.db_pcr_backend.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ic.ac.uk.db_pcr_backend.Constant;

@Service
public class GitLabService {

    public ResponseEntity<?> getRepositoryData(String repoUrl) {

        String url = Constant.GITLAB_API_BASE_URL + "/projects/:id/repository/tree";

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        System.out.println("Response: " + response.getBody());

        return ResponseEntity.ok(response.getBody());
    }

}
