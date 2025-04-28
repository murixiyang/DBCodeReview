package ic.ac.uk.db_pcr_backend;

public class Constant {
    public static final String GERRIT_BASE_URL = System.getenv().get("GERRIT_URL");
    public static final String GERRIT_AUTHENTICATE_URL = System.getenv().get("GERRIT_AUTHENTICATE_URL");

    public static final String ADMIN_USERNAME = "admin";
    public static final String USERNAME = "murixiyang";
    public static final String ADMIN_PASSWORD = "CwutpZjWKvgnRZf8hPdep8LlpYeu7lIhmZmjxaGVKA";
    public static final String USER_PASSWORD = "PXTx/6foa7mo7Hkl1LNsG2wlhdUd7wJZ6grtgoTjLQ";

    // public static final String GITLAB_API_BASE_URL =
    // "https://gitlab.doc.ic.ac.uk/api/v4";

    public static final String GITLAB_API_BASE_URL = System.getenv().get("GITLAB_URL");
    public static final String GITLAB_PERSONAL_TOKEN = "glpat-Smx_zxbc8UJQKLHzZqu6";

    public static String getGerritBaseUrl(Boolean needAuth) {
        return needAuth ? GERRIT_AUTHENTICATE_URL : GERRIT_BASE_URL;
    }

}
