package ic.ac.uk.db_pcr_backend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ic.ac.uk.db_pcr_backend.Constant;
import ic.ac.uk.db_pcr_backend.model.ProjectInfoModel;
import ic.ac.uk.db_pcr_backend.model.ChangeInfoModel;
import ic.ac.uk.db_pcr_backend.model.CommentInfoModel;
import ic.ac.uk.db_pcr_backend.model.DiffInfoModel;
import ic.ac.uk.db_pcr_backend.model.FileInfoModel;

@Service
public class GerritService {
    private final RestTemplate restTemplate;

    public GerritService() {
        restTemplate = new RestTemplate();
    }

    public Map<String, ProjectInfoModel> getProjectList() {
        String endPoint = "/projects";

        return fetchGerritMapData(endPoint, ProjectInfoModel[].class);
    }

    public List<ChangeInfoModel> getChangesWithQuery(String query) {
        String endPoint = "/changes?q=" + query;

        return fetchGerritListData(endPoint, ChangeInfoModel[].class);
    }

    public Map<String, FileInfoModel> getModifiedFileInChange(String changeId, String revisionId) {
        String endPoint = "/changes/" + changeId + "/revisions/" + revisionId + "/files";

        return fetchGerritMapData(endPoint, FileInfoModel[].class);
    }

    public CommentInfoModel putDraftComment(String changeId, String revisionId, String filePath,
            CommentInfoModel draftComment) {
        String endPoint = "/changes/" + changeId + "/revisions/" + revisionId + "/drafts";

        try {
            String url = Constant.GERRIT_BASE_URL + endPoint;

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(draftComment);

            ResponseEntity<String> response = restTemplate.postForEntity(url, json, String.class);
            if (response.getBody() == null) {
                return null;
            }

            json = CommonFunctionService.trimJson(response.getBody());
            return mapper.readValue(json, CommentInfoModel.class);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to put draft comment to Gerrit at endpoint: " + endPoint);
            e.printStackTrace();
            return null;
        }
    }

    public DiffInfoModel getDiffInFile(String changeId, String revisionId, String filePath) {
        String endPoint = "/changes/" + changeId + "/revisions/" + revisionId + "/files/" + filePath + "/diff";

        return fetchGerritData(endPoint, DiffInfoModel.class);
    }

    private <T> T fetchGerritData(String endpoint, Class<T> dataClass) {
        try {
            String url = Constant.GERRIT_BASE_URL + endpoint;

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getBody() == null) {
                return null;
            }

            String json = CommonFunctionService.trimJson(response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, dataClass);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to fetch data from Gerrit at endpoint: " + endpoint);
            e.printStackTrace();
            return null;
        }
    }

    private <T> List<T> fetchGerritListData(String endpoint, Class<T[]> dataClass) {
        try {
            String url = Constant.GERRIT_BASE_URL + endpoint;

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getBody() == null) {
                return Collections.emptyList();
            }

            String json = CommonFunctionService.trimJson(response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            T[] data = mapper.readValue(json, dataClass);
            return Arrays.asList(data);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to fetch data from Gerrit at endpoint: " + endpoint);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private <T> Map<String, T> fetchGerritMapData(String endpoint, Class<T[]> dataClass) {
        try {
            String url = Constant.GERRIT_BASE_URL + endpoint;

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getBody() == null) {
                return Collections.emptyMap();
            }

            String json = CommonFunctionService.trimJson(response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            Map<String, T> dataMap = mapper.readValue(json, new TypeReference<Map<String, T>>() {
            });

            return dataMap;
        } catch (IOException e) {
            System.err.println("ERROR: Failed to fetch data from Gerrit at endpoint: " + endpoint);
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

}
