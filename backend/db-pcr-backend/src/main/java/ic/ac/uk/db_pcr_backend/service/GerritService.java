package ic.ac.uk.db_pcr_backend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ic.ac.uk.db_pcr_backend.Constant;
import ic.ac.uk.db_pcr_backend.model.CommitInfo;
import ic.ac.uk.db_pcr_backend.model.ProjectInfoModel;
import ic.ac.uk.db_pcr_backend.model.CommitInfo.Owner;

@Service
public class GerritService {
    private final RestTemplate restTemplate;

    public GerritService() {
        restTemplate = new RestTemplate();
    }

    // Endpoint: GET /projects/
    public List<ProjectInfoModel> getProjectList() {
        try {
            String url = Constant.GERRIT_BASE_URL + "/projects/";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getBody() == null) {
                return Collections.emptyList();
            }

            // Strip prefix before parsing
            String json = response.getBody();

            System.out.println("json: " + json);

            if (json.startsWith(")]}'")) {
                json = json.substring(json.indexOf('\n')).trim();
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, ProjectInfoModel> projectMap = mapper.readValue(json,
                    new TypeReference<Map<String, ProjectInfoModel>>() {
                    });

            return new ArrayList<>(projectMap.values());
        } catch (IOException e) {
            System.err.println("ERROR: Failed to fetch project list from Gerrit.");

            // Log the error
            e.printStackTrace();

            // Return a safe fallback
            return Collections.emptyList();
        }
    }

    // public List<GerritChange> getChanges(String query) throws IOException {
    // // Example endpoint: GET /changes/?q=<query>
    // // E.g., query = "status:open" or "project:MyProject"

    // String url = GERRIT_BASE_URL + "/changes/?q=" + query;
    // ResponseEntity<String> response = restTemplate.getForEntity(url,
    // String.class);

    // if (response.getBody() == null) {
    // return Collections.emptyList();
    // }

    // // Gerrit typically prefixes JSON with 4 garbage chars: ")]}'"
    // // We need to strip them off before parsing
    // String json = response.getBody();
    // if (json.startsWith(")]}'")) {
    // json = json.substring(json.indexOf('\n')).trim();
    // }

    // // Parse JSON into a list of GerritChange objects
    // ObjectMapper mapper = new ObjectMapper();
    // return Arrays.asList(mapper.readValue(json, GerritChange[].class));
    // }

    public List<CommitInfo> getOriginalCommitList() {
        // 1. Use a simple HTTP client (like Spring's RestTemplate or OkHttp)
        // 2. Call Gerrit's REST endpoint, e.g. GET /changes/?q=status:open
        // 3. Parse JSON into a List<ChangeInfo> (use Jackson or GSON)

        List<CommitInfo> commits = new ArrayList<>();

        // Return a example Info for illustration
        CommitInfo exampleCommit = new CommitInfo();
        exampleCommit.id = "123";
        Owner examplOwner = new Owner();
        examplOwner.name = "Bob";
        examplOwner.email = "Bob@puppy.com";
        exampleCommit.owner = examplOwner;

        commits.add(exampleCommit);

        return commits;

        // return masked or unmasked data as needed

    }

    public List<CommitInfo> getAnonymousCommitList() {
        // 1. Use a simple HTTP client (like Spring's RestTemplate or OkHttp)
        // 2. Call Gerrit's REST endpoint, e.g. GET /changes/?q=status:open
        // 3. Parse JSON into a List<ChangeInfo> (use Jackson or GSON)

        return maskAuthors(getOriginalCommitList());
    }

    private List<CommitInfo> maskAuthors(List<CommitInfo> commits) {
        for (CommitInfo commit : commits) {
            // Example mask:
            commit.owner.name = "Anonymous Student";
            commit.owner.email = "anonymized@mycourse.edu";
        }
        return commits;
    }
}
