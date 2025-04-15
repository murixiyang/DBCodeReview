package ic.ac.uk.db_pcr_backend.model.GerritModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectInfoModel {
    public String id;
    public String state;
}