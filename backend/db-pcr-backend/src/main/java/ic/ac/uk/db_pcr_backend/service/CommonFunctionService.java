package ic.ac.uk.db_pcr_backend.service;

public class CommonFunctionService {

    public static String trimJson(String json) {
        if (json.startsWith(")]}'")) {
            json = json.substring(json.indexOf('\n')).trim();
        }
        return json;
    }

}
