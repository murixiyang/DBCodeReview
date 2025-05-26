package ic.ac.uk.db_pcr_backend.redactor;

import java.util.List;
import java.util.regex.Pattern;

public class Redactor {
    /**
     * Replace each exact occurrence of any blockNames in the list
     * (case-insensitive) with the fixed placeholder "***".
     */
    public static String redact(String text, List<String> blockNames) {
        if (text == null || text.isEmpty() || blockNames == null || blockNames.isEmpty()) {
            return text;
        }
        String result = text;
        for (String name : blockNames) {
            // (?i) case‐insensitive
            // (?<!\p{L}) ensure not preceded by a letter
            // (?!\p{L}) ensure not followed by a letter
            String regex = "(?i)(?<!\\p{L})"
                    + Pattern.quote(name)
                    + "(?!\\p{L})";
            result = result.replaceAll(regex, "***");
        }
        return result;
    }
}