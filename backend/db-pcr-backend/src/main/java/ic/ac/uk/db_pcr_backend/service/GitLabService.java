package ic.ac.uk.db_pcr_backend.service;

import java.net.URI;
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
    public List<Project> getPersonalProject(String oauthToken) throws GitLabApiException {
        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            return gitLabApi.getProjectApi().getOwnedProjects();
        }
    }

    /** Get the list of projects for a given group */
    public List<Project> getGroupProjects(String groupId, String oauthToken) throws GitLabApiException {
        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            return gitLabApi.getGroupApi().getProjects(groupId);
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

    /** Get project by project id */
    public Project getProjectById(String projectId, String oauthToken) throws GitLabApiException {
        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            return gitLabApi.getProjectApi().getProject(projectId);
        }
    }

    /** Get git clone URL for a project id */
    public String getProjectCloneUrl(String projectId, String oauthToken) throws GitLabApiException {
        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            Project project = gitLabApi.getProjectApi().getProject(projectId);
            URI uri = URI.create(project.getHttpUrlToRepo());
            String path = uri.getRawPath();

            // Append port to the path
            return apiUrl + path;
        }
    }

    /** Get git project name by pathWithNamespace */
    public String getProjectPathWithNamespace(String projectid, String oauthToken) throws GitLabApiException {
        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            Project project = gitLabApi.getProjectApi().getProject(projectid);
            return project.getPathWithNamespace();
        }
    }

}
