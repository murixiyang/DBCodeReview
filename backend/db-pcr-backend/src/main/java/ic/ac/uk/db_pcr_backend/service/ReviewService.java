package ic.ac.uk.db_pcr_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gerrit.extensions.restapi.RestApiException;

@Service
public class ReviewService {

    @Autowired
    private GerritService gerritSvc;

    // /** Fetch commits list using Uuid */
    // public List<ChangeInfoDto> fetchCommitsForAssignment(String assignmentUuid)
    // throws Exception {
    // ReviewAssignmentEntity asn =
    // reviewAssignmentRepo.findByAssignmentUuid(assignmentUuid)
    // .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    // // build the path
    // String repoPath = asn.getAuthorName() + "/" + asn.getProjectName();

    // // call Gerrit
    // List<ChangeInfo> changes = gerritSvc.getCommitsFromProjectPath(repoPath);

    // return changes.stream()
    // .map(change -> {

    // return new ChangeInfoDto(
    // change.changeId,
    // change.subject,
    // change.updated.toInstant());
    // })
    // .collect(Collectors.toList());
    // }

    // * Get ChangeDiff by changeId */
    public String getDiffs(String gerritChangeId) throws RestApiException {
        System.out.println("Service: ReviewService.getDiffs");

        return gerritSvc.fetchRawPatch(gerritChangeId, "current");
    }

}
