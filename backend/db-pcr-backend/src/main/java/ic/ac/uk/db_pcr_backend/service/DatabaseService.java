package ic.ac.uk.db_pcr_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.dto.ReviewStatusDto;
import ic.ac.uk.db_pcr_backend.entity.ReviewStatusEntity;
import ic.ac.uk.db_pcr_backend.repository.ReviewStatusRepository;

@Service
public class DatabaseService {

    @Autowired
    private ReviewStatusRepository reviewStatusRepo;

    public List<ReviewStatusEntity> getReviewStatuses(String username, String projectId) {
        return reviewStatusRepo.findByUsernameAndProjectId(username, projectId);
    }

    public ReviewStatusEntity createReviewStatus(ReviewStatusDto dto) {
        ReviewStatusEntity status = new ReviewStatusEntity();
        status.setUsername(dto.getUsername());
        status.setProjectId(dto.getProjectId());
        status.setCommitSha(dto.getCommitSha());
        status.setStatus(dto.getStatus());
        status.setLastUpdated(LocalDateTime.now());

        return reviewStatusRepo.save(status);
    }

}
