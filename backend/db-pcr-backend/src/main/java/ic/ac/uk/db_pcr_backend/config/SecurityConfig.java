package ic.ac.uk.db_pcr_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // disable CSRF for a pure JSON API
        .csrf(csrf -> csrf.disable())

        // require Basic auth on /api/**, allow all other endpoints
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/**").authenticated()
            .anyRequest().permitAll())

        // configure HTTP Basic and override its entry point:
        .httpBasic(basic -> basic
            .authenticationEntryPoint(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

    return http.build();
  }

  @Bean
  public InMemoryUserDetailsManager userDetailsService() {
    var alice = User.withUsername("alice")
        .password("{noop}alicePass")
        .roles("USER")
        .build();
    var bob = User.withUsername("bob")
        .password("{noop}bobPass")
        .roles("USER")
        .build();
    return new InMemoryUserDetailsManager(alice, bob);
  }
}