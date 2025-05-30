package ic.ac.uk.db_pcr_backend.dto.eval;

public class NamedAuthorCodeDto {

    private String gerritChangeId;
    private String language;
    private String displayName; // Username when identified, or pseudonym when anonymous

    public NamedAuthorCodeDto(String gerritChangeId, String language, String displayName) {
        this.gerritChangeId = gerritChangeId;
        this.language = language;
        this.displayName = displayName;
    }

    public String getGerritChangeId() {
        return gerritChangeId;
    }

    public void setGerritChangeId(String gerritChangeId) {
        this.gerritChangeId = gerritChangeId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}
