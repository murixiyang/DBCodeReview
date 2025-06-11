package ic.ac.uk.db_pcr_backend.service;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Diff;
import org.gitlab4j.api.models.Group;
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.User;
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

    /** Get the list of groups for a given user. */
    public List<Group> getGroups(String oauthToken) throws GitLabApiException {
        System.out.println("Service: GitLabService.getGroups");

        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            return gitLabApi.getGroupApi().getGroups();
        }
    }

    /** Get the list of projects for a given user. */
    public List<Project> getProject(String oauthToken) throws GitLabApiException {
        System.out.println("Service: GitLabService.getProject");

        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            return gitLabApi.getProjectApi().getProjects();
        }
    }

    /** Get the list of personal projects for a given user. */
    public List<Project> getPersonalProject(String oauthToken) throws GitLabApiException {
        System.out.println("Service: GitLabService.getPersonalProject");

        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            return gitLabApi.getProjectApi().getOwnedProjects();
        }
    }

    /** Get the list of projects for a given group */
    public List<Project> getGroupProjects(String groupId, String oauthToken) throws GitLabApiException {
        System.out.println("Service: GitLabService.getGroupProjects");

        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            return gitLabApi.getGroupApi().getProjects(groupId);
        }
    }

    /** Get user by user id */
    public User getUserById(Long userId, String oauthToken) throws GitLabApiException {
        System.out.println("Service: GitLabService.getUserById");

        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            return gitLabApi.getUserApi().getUser(userId);
        }
    }

    /** Get the list of commits for a given project. */
    public List<Commit> getProjectCommits(Long gitlabProjectId, String oauthToken) throws GitLabApiException {
        System.out.println("Service: GitLabService.getProjectCommits");

        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            List<Commit> commits = gitLabApi.getCommitsApi().getCommits(gitlabProjectId);
            return commits;
        }
    }

    /** Get the unidiff for a given commit. */
    public List<Diff> getCommitDiff(String projectId, String sha, String oauthToken) throws GitLabApiException {
        System.out.println("Service: GitLabService.getCommitDiff");

        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            List<Diff> diff = gitLabApi.getCommitsApi().getDiff(projectId, sha);
            return diff;
        }
    }

    /** Get project by project id */
    public Project getProjectById(String projectId, String oauthToken) throws GitLabApiException {
        System.out.println("Service: GitLabService.getProjectById");

        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            return gitLabApi.getProjectApi().getProject(projectId);
        }
    }

    /** Get project by project id in a group */
    public Project getGroupProjectById(String groupId, String projectId, String oauthToken) throws GitLabApiException {
        System.out.println("Service: GitLabService.getGroupProjectById");

        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            return gitLabApi.getGroupApi().getProjects(groupId).stream()
                    .filter(project -> project.getId().toString().equals(projectId))
                    .findFirst()
                    .orElse(null);
        }
    }

    /** Get git clone URL for a project id */
    public String getProjectCloneUrl(String projectId, String oauthToken) throws GitLabApiException {
        System.out.println("Service: GitLabService.getProjectCloneUrl");

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
        System.out.println("Service: GitLabService.getProjectPathWithNamespace");

        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            Project project = gitLabApi.getProjectApi().getProject(projectid);
            return project.getPathWithNamespace();
        }
    }

    /** Get student list */
    public List<Member> getDevInGroup(String groupId, String oauthToken) throws GitLabApiException {
        System.out.println("Service: GitLabService.getDevInGroup");

        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            List<Member> members = gitLabApi.getGroupApi().getMembers(groupId);

            return members.stream()
                    .filter(m -> m.getAccessLevel().value == AccessLevel.DEVELOPER.value)
                    .collect(Collectors.toList());
        }
    }

    /** Get group access level */
    public AccessLevel getGroupAccessLevel(Long groupId, Long userId, String oauthToken) throws GitLabApiException {
        System.out.println("Service: GitLabService.getGroupAccessLevel");

        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            Member member = gitLabApi.getGroupApi().getMember(groupId, userId);
            return member.getAccessLevel();
        }
    }

    /** Get groups with maintainer access */
    public List<Group> getGroupsWithMaintainerAccess(Long userId, String oauthToken) throws GitLabApiException {
        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, oauthToken)) {
            return gitLabApi.getGroupApi().getGroups()
                    .stream()
                    .filter(group -> {
                        try {
                            Member currentUser = gitLabApi.getGroupApi().getMember(group, userId);
                            return currentUser.getAccessLevel().value >= AccessLevel.MAINTAINER.value;
                        } catch (GitLabApiException e) {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

        }

    }

    /** Check if the current user has maintainer access to project group */
    public boolean hasMaintainerAccess(Long gitlabProjectId, Long userId, String accessToken) {
        System.out.println("Service: GitLabService.hasMaintainerAccess");
        try (GitLabApi gitLabApi = new GitLabApi(apiUrl, TokenType.OAUTH2_ACCESS, accessToken)) {
            // Check group level access
            Project project = gitLabApi.getProjectApi().getProject(gitlabProjectId);
            if (project.getNamespace() != null && "group".equals(project.getNamespace().getKind())) {
                Long groupId = project.getNamespace().getId();
                Member groupMember = gitLabApi.getGroupApi().getMember(groupId, userId);
                return groupMember != null && groupMember.getAccessLevel().value >= AccessLevel.MAINTAINER.value;
            }
        } catch (GitLabApiException e) {
            e.printStackTrace();
        }
        return false;
    }

}
