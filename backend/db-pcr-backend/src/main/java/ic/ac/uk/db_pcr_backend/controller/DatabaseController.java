package ic.ac.uk.db_pcr_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ic.ac.uk.db_pcr_backend.service.DatabaseService;

@RestController
@RequestMapping("/api")
public class DatabaseController {

    @Autowired
    private DatabaseService databaseSvc;

    @GetMapping("/get-review-status")
    public List<ReviewStatusEntity> getReviewStatuses(
            @RequestParam String username,
            @RequestParam String projectId) {
        return databaseSvc.getReviewStatuses(username, projectId);
    }

    @PostMapping("/create-review-status")
    public ReviewStatusEntity createReviewStatus(@RequestBody ReviewStatusDto dto) {
        return databaseSvc.createReviewStatus(dto);
    }

    @PutMapping("/update-review-status")
    public ReviewStatusEntity updateReviewStatus(@RequestBody ReviewStatusDto dto) {
        return databaseSvc.updateReviewStatus(dto);
    }

}
