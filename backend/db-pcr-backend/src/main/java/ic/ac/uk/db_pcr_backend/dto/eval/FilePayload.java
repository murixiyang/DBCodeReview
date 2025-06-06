package ic.ac.uk.db_pcr_backend.dto.eval;

public class FilePayload {

    private String name;
    private String content;

    public FilePayload() {
    }

    public FilePayload(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
