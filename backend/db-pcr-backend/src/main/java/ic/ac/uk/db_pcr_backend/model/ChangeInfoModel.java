package ic.ac.uk.db_pcr_backend.model;

public class ChangeInfoModel {
    public String id; // '<project>\~<_number>'
    public String triplet_id; // '<project>~<branch>~<Change-Id>'
    public String project;
    public String branch;
    public String change_id;
    public String subject;
    public String status;
    public String created;
    public String updated;
    public String insertions;
    public String deletions;
    public String owner;
}
