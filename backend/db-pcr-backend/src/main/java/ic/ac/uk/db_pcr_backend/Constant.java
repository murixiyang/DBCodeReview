package ic.ac.uk.db_pcr_backend;

public class Constant {
    public static final String GERRIT_BASE_URL = "http://localhost:8080";
    public static final String GERRIT_AUTHENTICATE_URL = "http://localhost:8080/a";
    public static final String ADMIN_USERNAME = "admin";
    public static final String USERNAME = "murixiyang";
    public static final String ADMIN_PASSWORD = "3v3nyu7W7qpYhf64Q4fp9RZq++xGf8dzMN/rYihzwA";
    public static final String USER_PASSWORD = "PXTx/6foa7mo7Hkl1LNsG2wlhdUd7wJZ6grtgoTjLQ";

    public static final String GITLAB_API_BASE_URL = "https://gitlab.doc.ic.ac.uk/api/v4";
    public static final String GITLAB_PERSONAL_TOKEN = "glpat-Smx_zxbc8UJQKLHzZqu6";

    public static String getGerritBaseUrl(Boolean needAuth) {
        return needAuth ? GERRIT_AUTHENTICATE_URL : GERRIT_BASE_URL;
    }

}
