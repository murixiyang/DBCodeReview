package ic.ac.uk.db_pcr_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ic.ac.uk.db_pcr_backend.entity.UserEntity;
import ic.ac.uk.db_pcr_backend.repository.UserRepo;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepo;

    public UserEntity getOrCreateUserByName(Long gitlabUserId, String username) {
        return userRepo.findByUsername(username)
                .orElseGet(() -> userRepo.save(new UserEntity(gitlabUserId, username, null)));
    }

    public UserEntity getOrCreateUserByGitlabId(Long gitlabUserId, String username) {
        return userRepo.findByGitlabUserId(gitlabUserId)
                .orElseGet(() -> userRepo.save(new UserEntity(gitlabUserId, username, null)));
    }

    public UserEntity getOrExceptionUserById(Long Id) {
        return userRepo.findById(Id)
                .orElseThrow(() -> new IllegalStateException("User not found, id: " + Id));
    }

    public UserEntity getOrExceptionUserByName(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found, username: " + username));
    }

    public UserEntity getOrExceptionUserByGitlabId(Long gitlabUserId) {
        return userRepo.findByGitlabUserId(gitlabUserId)
                .orElseThrow(() -> new IllegalStateException("User not found, gitlab user id: " + gitlabUserId));
    }

}
