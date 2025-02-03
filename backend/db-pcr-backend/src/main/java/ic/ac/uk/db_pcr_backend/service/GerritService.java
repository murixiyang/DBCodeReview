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
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ic.ac.uk.db_pcr_backend.Constant;
import ic.ac.uk.db_pcr_backend.model.ProjectInfoModel;
import ic.ac.uk.db_pcr_backend.model.ChangeInfoModel;
import ic.ac.uk.db_pcr_backend.model.ModiFileInfoModel;

@Service
public class GerritService {
    private final RestTemplate restTemplate;

    public GerritService() {
        restTemplate = new RestTemplate();
    }

    public List<ProjectInfoModel> getProjectList() {
        String endPoint = "/projects";

        return fetchGerritMapData(endPoint, ProjectInfoModel[].class);
    }

    public List<ChangeInfoModel> getChangesWithQuery(String query) {
        String endPoint = "/changes?q=" + query;

        return fetchGerritListData(endPoint, ChangeInfoModel[].class);
    }

    public List<ModiFileInfoModel> getModifiedFileInChange(String changeId, String revisionId) {
        String endPoint = "/changes/" + changeId + "/revisions/" + revisionId + "/files";

        return fetchGerritMapData(endPoint, ModiFileInfoModel[].class);
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

    private <T> List<T> fetchGerritMapData(String endpoint, Class<T[]> dataClass) {
        try {
            String url = Constant.GERRIT_BASE_URL + endpoint;

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getBody() == null) {
                return Collections.emptyList();
            }

            String json = CommonFunctionService.trimJson(response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            Map<String, T> dataMap = mapper.readValue(json, new TypeReference<Map<String, T>>() {
            });

            return new ArrayList<>(dataMap.values());
        } catch (IOException e) {
            System.err.println("ERROR: Failed to fetch data from Gerrit at endpoint: " + endpoint);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}
