package ic.ac.uk.db_pcr_backend.redactor;

import java.util.List;
import java.util.regex.Pattern;

public class Redactor {
    /**
     * Replace each exact occurrence of any blockNames in the list
     * (case-insensitive, whole-word) with the fixed placeholder "***".
     */
    public static String redact(String text, List<String> blockNames) {
        if (text == null || text.isEmpty() || blockNames == null || blockNames.isEmpty()) {
            return text;
        }
        String result = text;
        for (String nameText : blockNames) {
            // \b = word boundary, (?i) = case-insensitive
            String regex = "(?i)\\b" + Pattern.quote(nameText) + "\\b";
            result = result.replaceAll(regex, "***");
        }
        return result;
    }
}