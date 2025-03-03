package ic.ac.uk.db_pcr_backend.model.GitLabModel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabCommitModel {
    String id;
    String short_id;
    String title;
    String message;
    String author_name;
    String author_email;
    String commited_date;
    String web_url;
}