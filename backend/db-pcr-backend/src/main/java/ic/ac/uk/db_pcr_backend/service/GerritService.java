package ic.ac.uk.db_pcr_backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.model.CommitInfo;
import ic.ac.uk.db_pcr_backend.model.CommitInfo.Owner;

@Service
public class GerritService {




    public List<CommitInfo> getOriginalCommitList() {
        // 1. Use a simple HTTP client (like Spring's RestTemplate or OkHttp)
        // 2. Call Gerrit's REST endpoint, e.g. GET /changes/?q=status:open
        // 3. Parse JSON into a List<ChangeInfo> (use Jackson or GSON)

        List<CommitInfo> commits = new ArrayList<>();

        // Return a example Info for illustration
        CommitInfo exampleCommit = new CommitInfo();
        exampleCommit.id = "123";
        Owner examplOwner = new Owner(); 
        examplOwner.name = "Bob";
        examplOwner.email = "Bob@puppy.com";
        exampleCommit.owner = examplOwner;

        commits.add(exampleCommit);

        return commits;

        // return masked or unmasked data as needed
        
    }

    public List<CommitInfo> getAnonymousCommitList(){
        // 1. Use a simple HTTP client (like Spring's RestTemplate or OkHttp)
        // 2. Call Gerrit's REST endpoint, e.g. GET /changes/?q=status:open
        // 3. Parse JSON into a List<ChangeInfo> (use Jackson or GSON)

        return maskAuthors(getOriginalCommitList());
    }

    private List<CommitInfo> maskAuthors(List<CommitInfo> commits) {
        for (CommitInfo commit : commits) {
            // Example mask:
            commit.owner.name = "Anonymous Student";
            commit.owner.email = "anonymized@mycourse.edu";
        }
        return commits;
    }
}
