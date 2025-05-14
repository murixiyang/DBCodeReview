package ic.ac.uk.db_pcr_backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ic.ac.uk.db_pcr_backend.entity.ChangeRequestEntity;
import ic.ac.uk.db_pcr_backend.entity.ProjectEntity;
import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.repository.ChangeRequestRepo;
import ic.ac.uk.db_pcr_backend.repository.GitlabGroupRepo;
import ic.ac.uk.db_pcr_backend.repository.ProjectRepo;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;
import jakarta.persistence.EntityNotFoundException;

@Service
public class DatabaseService {

    @Autowired
    private GitLabService gitLabSvc;

    @Autowired
    private ChangeRequestRepo changeRequestRepo;

    @Autowired
    private ProjectRepo projectRepo;

    @Autowired
    private GitlabGroupRepo groupRepo;

    @Autowired
    private UserRepo userRepo;

    @Transactional
    public void syncPersonalProjects(Long gitlabUserId, String accessToken)
            throws GitLabApiException {
        // 1) Ensure the User record exists
        UserEntity user = userRepo.findByGitlabUserId(gitlabUserId)
                .orElseGet(() -> userRepo.save(new UserEntity(gitlabUserId, "", null)));

        // 2) Call GitLab’s API for personal projects
        List<Project> forks = gitLabSvc.getPersonalProject(accessToken);

        // Check if project exists in the database
        for (var dto : forks) {
            ProjectEntity p = projectRepo.findByGitlabProjectId(dto.getId())
                    .orElseGet(() -> new ProjectEntity(dto.getId(), dto.getName(), dto.getNamespace().toString()));

            p.setOwner(user);

            projectRepo.save(p);
        }
    }

    public List<ChangeRequestEntity> getChangeRequesets(String username, String projectId) {
        return changeRequestRepo.findByUsernameAndProjectId(username, projectId);
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
