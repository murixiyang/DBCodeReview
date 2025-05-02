package ic.ac.uk.db_pcr_backend.service;

import java.util.List;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Diff;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.models.Constants.TokenType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;

import org.springframework.stereotype.Service;

@Service
public class GitLabService {

    private final String apiUrl;

    // Constructor
    public GitLabService(@Value("${gitlab.url}") String apiUrl,
            RestTemplateBuilder restTemplateBuilder) {
        this.apiUrl = apiUrl;
    }

    /** Get the list of projects for a given user. */
    public List<Project> listPersonalProject(String oauthToken) throws GitLabApiException {
        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            return gitLabApi.getProjectApi().getOwnedProjects();
        }
    }

    /** Get the list of commits for a given project. */
    public List<Commit> getProjectCommits(String projectId, String oauthToken) throws GitLabApiException {
        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            List<Commit> commits = gitLabApi.getCommitsApi().getCommits(projectId);
            return commits;
        }
    }

    /** Get the unidiff for a given commit. */
    public List<Diff> getCommitDiff(String projectId, String sha, String oauthToken) throws GitLabApiException {
        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            List<Diff> diff = gitLabApi.getCommitsApi().getDiff(projectId, sha);
            return diff;
        }
    }
}
