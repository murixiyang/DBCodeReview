package ic.ac.uk.db_pcr_backend.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthTestController {

    /** Get Current User */
    @GetMapping("/user")
    public ResponseEntity<String> currentUser(Principal principal) {
        // returns the username of the logged-in user
        return principal != null
                ? ResponseEntity.ok(principal.getName())
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
