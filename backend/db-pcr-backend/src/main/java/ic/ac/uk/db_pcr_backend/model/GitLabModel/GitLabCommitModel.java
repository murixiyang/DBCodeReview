package ic.ac.uk.db_pcr_backend.model.GitLabModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabCommitModel {
    public String id;
    public String short_id;
    public String title;
    public String message;
    public String author_name;
    public String author_email;
    public String committed_date;
    public String web_url;
}