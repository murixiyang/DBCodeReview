package ic.ac.uk.db_pcr_backend.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import ic.ac.uk.db_pcr_backend.Constant;
import ic.ac.uk.db_pcr_backend.model.GitLabModel.GitLabCommitModel;

@Service
public class GitLabService {

    public ResponseEntity<List<GitLabCommitModel>> getRepositoryCommits(String repoUrl) {
        String encodedProject = getEncodedProject(repoUrl);
        String endpoint = Constant.GITLAB_API_BASE_URL + "/projects/" + encodedProject + "/repository/commits";

        try {
            URI url = new URI(endpoint);

            HttpHeaders headers = new HttpHeaders();
            headers.set("PRIVATE-TOKEN", Constant.GITLAB_PERSONAL_TOKEN);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            if (response.getBody() == null) {
                return ResponseEntity.ok().body(Collections.emptyList());
            }

            String json = CommonFunctionService.trimJson(response.getBody());

            System.out.println("json: " + json);

            ObjectMapper mapper = new ObjectMapper();
            GitLabCommitModel[] data = mapper.readValue(json, GitLabCommitModel[].class);

            return ResponseEntity.ok().body(Arrays.asList(data));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("ERROR: Failed to fetch data from GitLab at endpoint: " + endpoint);
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

    }

    private String getEncodedProject(String url) {
        // https://gitlab.doc.ic.ac.uk/ly1021/dbcrtestproject01 ->
        // ly1021%2Fdbcrtestproject01

        url = url.replace("https://gitlab.doc.ic.ac.uk/", "");

        return UriUtils.encode(url, StandardCharsets.UTF_8);
    }

}
