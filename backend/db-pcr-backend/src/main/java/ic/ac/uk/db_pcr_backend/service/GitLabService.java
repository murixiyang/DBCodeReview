package ic.ac.uk.db_pcr_backend.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
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

    private final String apiUrl;
    private final String token;
    private final String webhookUrl;
    private final RestTemplate restTemplate;

    // Constructor
    public GitLabService(@Value("${gitlab.url}") String apiUrl,
            @Value("${gitlab.token}") String token,
            @Value("${webhook.url}") String webhookUrl,
            RestTemplateBuilder restTemplateBuilder) {
        this.apiUrl = apiUrl;
        this.token = token;
        this.webhookUrl = webhookUrl;
        this.restTemplate = restTemplateBuilder.defaultHeader("PRIVATE-TOKEN", token).build();

    }

    /** Ensure that a push-only webhook is set up for the given project. */
    public void ensurePushWebhook(int projectId) {
        var hooks = listWebhooks(projectId);
        boolean exists = hooks.stream()
                .anyMatch(h -> webhookUrl.equals(h.get("url")) && Boolean.TRUE.equals(h.get("push_events")));
        if (!exists) {
            addPushWebhook(projectId);
        }
    }

    /** Add a push-only webhook to given project */
    public void addPushWebhook(int projectId) {
        String url = apiUrl + "/projects/" + projectId + "/hooks";
        var payload = Map.of(
                "url", webhookUrl,
                "push_events", true,
                "merge_requests_events", false,
                "enable_ssl_verification", false);
        restTemplate.postForEntity(url, payload, String.class);
    }

    /** List existing hooks on a project */
    public List<Map<String, Object>> listWebhooks(int projectId) {
        String url = apiUrl + "/projects/" + projectId + "/hooks";
        return restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                }).getBody();
    }

    /** Remove a webhook by its hookId. */
    public void removeWebhook(int projectId, int hookId) {
        var url = apiUrl + "/projects/" + projectId + "/hooks/" + hookId;
        restTemplate.delete(url);
    }

    /** Get the list of commits for a given repository URL. */
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

    /** Get utf8 encoded project url */
    private String getEncodedProject(String url) {
        // https://gitlab.doc.ic.ac.uk/ly1021/dbcrtestproject01 ->
        // ly1021%2Fdbcrtestproject01

        url = url.replace("https://gitlab.doc.ic.ac.uk/", "");

        return UriUtils.encode(url, StandardCharsets.UTF_8);
    }

}
