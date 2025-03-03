package ic.ac.uk.db_pcr_backend.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserEntityService {
    @Autowired
    private UserEntityRepository userEntityRepository;

    public UserEntity saveUserToken(String username, String token) {
        UserEntity userToken = new UserEntity(username, token);
        return userEntityRepository.save(userToken);
    }

    public UserEntity getUserToken(Long id) {
        return userEntityRepository.findById(id).orElse(null);
    }

    public void deleteUserToken(Long id) {
        userEntityRepository.deleteById(id);
    }
}
