package ic.ac.uk.db_pcr_backend.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import ic.ac.uk.db_pcr_backend.Constant;
import ic.ac.uk.db_pcr_backend.model.GerritModel.ChangeInfoModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.CommentInfoModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.CommentInputModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.DiffInfoModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.FileInfoModel;
import ic.ac.uk.db_pcr_backend.model.GerritModel.ProjectInfoModel;

@Service
public class GerritService {
    private final RestTemplate restTemplate;

    public GerritService() {
        restTemplate = new RestTemplate();
    }

    public Map<String, ProjectInfoModel> getProjectList() {
        String endPoint = "/projects";

        return fetchGerritMapData(endPoint, ProjectInfoModel.class, false);
    }

    public List<ChangeInfoModel> getChangesWithQuery(String query) {
        String endPoint = "/changes?q=" + query;

        return fetchGerritListData(endPoint, ChangeInfoModel[].class, false);
    }

    public Map<String, FileInfoModel> getModifiedFileInChange(String changeId, String revisionId) {
        String endPoint = "/changes/" + changeId + "/revisions/" + revisionId + "/files";

        return fetchGerritMapData(endPoint, FileInfoModel.class, false);
    }

    public DiffInfoModel getDiffInFile(String changeId, String revisionId, String filePath) {
        String endPoint = "/changes/" + changeId + "/revisions/" + revisionId + "/files/" + filePath + "/diff";

        return fetchGerritData(endPoint, DiffInfoModel.class, false);
    }

    public Map<String, CommentInfoModel[]> getAllDraftComments(String changeId, String revisionId) {
        String endPoint = "/changes/" + changeId + "/revisions/" + revisionId + "/drafts";

        return fetchGerritMapData(endPoint, CommentInfoModel[].class, true);
    }

    public Map<String, CommentInfoModel[]> getAllComments(String changeId, String revisionId) {
        String endPoint = "/changes/" + changeId + "/revisions/" + revisionId + "/comments";

        return fetchGerritMapData(endPoint, CommentInfoModel[].class, false);
    }

    public ResponseEntity<CommentInfoModel> updateDraftComment(String changeId, String revisionId,
            CommentInputModel commentInput) {

        String endPoint = Constant.getGerritBaseUrl(true) + "/changes/" + changeId +
                "/revisions/" + revisionId + "/drafts/" + commentInput.id;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(Constant.ADMIN_USERNAME, Constant.ADMIN_PASSWORD);

        HttpEntity<CommentInputModel> requestEntity = new HttpEntity<>(commentInput, headers);

        try {

            ResponseEntity<String> response = restTemplate.exchange(
                    endPoint,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class);
            String json = CommonFunctionService.trimJson(response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            CommentInfoModel result = mapper.readValue(json, CommentInfoModel.class);

            return ResponseEntity.status(response.getStatusCode()).body(result);

        } catch (IOException e) {
            System.out.println("ERROR: Failed to put draft comment to Gerrit at endpoint: " + endPoint);
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<CommentInfoModel> putDraftComment(String changeId, String revisionId,
            CommentInputModel commentInput) {

        String endPoint = Constant.getGerritBaseUrl(true) + "/changes/" + changeId +
                "/revisions/" + revisionId + "/drafts";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(Constant.ADMIN_USERNAME, Constant.ADMIN_PASSWORD);

        HttpEntity<CommentInputModel> requestEntity = new HttpEntity<>(commentInput, headers);

        try {

            ResponseEntity<String> response = restTemplate.exchange(
                    endPoint,
                    HttpMethod.PUT,
                    requestEntity,
                    String.class);
            String json = CommonFunctionService.trimJson(response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            CommentInfoModel result = mapper.readValue(json, CommentInfoModel.class);

            return ResponseEntity.status(response.getStatusCode()).body(result);

        } catch (IOException e) {
            System.out.println("ERROR: Failed to put draft comment to Gerrit at endpoint: " + endPoint);
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    public ResponseEntity<String> deleteDraftComment(String changeId, String revisionId, String draftId) {
        String endPoint = Constant.getGerritBaseUrl(true) + "/changes/" + changeId +
                "/revisions/" + revisionId + "/drafts/" + draftId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(Constant.ADMIN_USERNAME, Constant.ADMIN_PASSWORD);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    endPoint,
                    HttpMethod.DELETE,
                    entity,
                    String.class);

            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            System.out.println("ERROR: Failed to delete draft comment from Gerrit at endpoint: " + endPoint);
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    private <T> T fetchGerritData(String endpoint, Class<T> dataClass, Boolean needAuth) {
        try {
            String url = Constant.getGerritBaseUrl(needAuth) + endpoint;

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

    private <T> List<T> fetchGerritListData(String endpoint, Class<T[]> dataClass, Boolean needAuth) {
        try {
            String url = Constant.getGerritBaseUrl(needAuth) + endpoint;

            HttpHeaders headers = new HttpHeaders();
            if (needAuth) {
                headers.setBasicAuth(Constant.ADMIN_USERNAME, Constant.ADMIN_PASSWORD);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() == null) {
                return Collections.emptyList();
            }

            String json = CommonFunctionService.trimJson(response.getBody());

            System.out.println("json: " + json);

            ObjectMapper mapper = new ObjectMapper();
            T[] data = mapper.readValue(json, dataClass);
            return Arrays.asList(data);
        } catch (IOException e) {
            System.err.println("ERROR: Failed to fetch data from Gerrit at endpoint: " + endpoint);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private <T> Map<String, T> fetchGerritMapData(String endpoint, Class<T> dataClass, Boolean needAuth) {
        try {
            String url = Constant.getGerritBaseUrl(needAuth) + endpoint;

            HttpHeaders headers = new HttpHeaders();
            if (needAuth) {
                headers.setBasicAuth(Constant.ADMIN_USERNAME, Constant.ADMIN_PASSWORD);
            }
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() == null) {
                return Collections.emptyMap();
            }

            String json = CommonFunctionService.trimJson(response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            Map<String, T> dataMap = mapper.readValue(
                    json,
                    mapper.getTypeFactory().constructMapType(Map.class, String.class, dataClass));

            return dataMap;
        } catch (IOException e) {
            System.err.println("ERROR: Failed to fetch data from Gerrit at endpoint: " + endpoint);
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

}
