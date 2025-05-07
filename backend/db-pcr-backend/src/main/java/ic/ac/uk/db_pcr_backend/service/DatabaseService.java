package ic.ac.uk.db_pcr_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.dto.ReviewStatusDto;
import ic.ac.uk.db_pcr_backend.entity.ReviewStatusEntity;
import ic.ac.uk.db_pcr_backend.repository.ReviewStatusRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class DatabaseService {

    @Autowired
    private ReviewStatusRepository reviewStatusRepo;

    public List<ReviewStatusEntity> getReviewStatuses(String username, String projectId) {
        return reviewStatusRepo.findByUsernameAndProjectId(username, projectId);
    }

    public ReviewStatusEntity createReviewStatus(ReviewStatusDto dto) {
        System.out.println(
                "DBLOG: Creating DTO: " + dto.getUsername() + ", " + dto.getProjectId() + ", " + dto.getCommitSha()
                        + ", " + dto.getReviewStatus());

        ReviewStatusEntity statusEntity = new ReviewStatusEntity();
        statusEntity.setUsername(dto.getUsername());
        statusEntity.setProjectId(dto.getProjectId());
        statusEntity.setCommitSha(dto.getCommitSha());
        statusEntity.setReviewStatus(dto.getReviewStatus());
        statusEntity.setLastUpdated(LocalDateTime.now());

        return reviewStatusRepo.save(statusEntity);
    }

    public ReviewStatusEntity updateReviewStatus(ReviewStatusDto dto) {
        System.out.println(
                "DBLOG: Updating DTO: " + dto.getUsername() + ", " + dto.getProjectId() + ", " + dto.getCommitSha()
                        + ", " + dto.getReviewStatus());

        ReviewStatusEntity statusEntity = reviewStatusRepo
                .findByUsernameAndProjectIdAndCommitSha(
                        dto.getUsername(), dto.getProjectId(), dto.getCommitSha())
                .orElseThrow(() -> new EntityNotFoundException("No status to update"));
        statusEntity.setReviewStatus(dto.getReviewStatus());
        statusEntity.setLastUpdated(LocalDateTime.now());
        return reviewStatusRepo.save(statusEntity);

    }

}
