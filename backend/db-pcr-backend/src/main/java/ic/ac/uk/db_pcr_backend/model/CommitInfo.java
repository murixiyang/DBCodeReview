package ic.ac.uk.db_pcr_backend.model;

public class CommitInfo {
    public String id;
    public Owner owner;


    public static class Owner {
        public String name;
        public String email;
    }

}
