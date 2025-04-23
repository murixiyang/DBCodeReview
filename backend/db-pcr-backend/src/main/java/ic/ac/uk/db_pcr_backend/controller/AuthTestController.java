package ic.ac.uk.db_pcr_backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthTestController {

    // Will only be callable if HTTP Basic creds are valid
    @GetMapping("/auth-test")
    public String authTest(@AuthenticationPrincipal UserDetails user) {
        return user.getUsername();
    }
}
