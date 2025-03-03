package ic.ac.uk.db_pcr_backend.database;

public class UserRepoService {
    private final UserEntityRepository userEntityRepository;
    private final UserRepoRepository userRepoRepository;

    public UserRepoService(UserEntityRepository userEntityRepository,
            UserRepoRepository userRepoRepository) {
        this.userEntityRepository = userEntityRepository;
        this.userRepoRepository = userRepoRepository;
    }

    public UserRepoEntity addRepositoryLink(Long userId, String repoUrl) {
        // Find the user first
        UserEntity user = userEntityRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create and save a new RepositoryLink
        UserRepoEntity link = new UserRepoEntity(repoUrl, user);
        return userRepoRepository.save(link);
    }

}
