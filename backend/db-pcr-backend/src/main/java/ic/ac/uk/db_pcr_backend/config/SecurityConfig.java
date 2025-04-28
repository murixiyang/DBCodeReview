package ic.ac.uk.db_pcr_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            // allow webhook freely
            .requestMatchers("/api/hooks/gitlab").permitAll()
            // secure all other /api/** behind login
            .requestMatchers("/api/**").authenticated()
            .anyRequest().permitAll())
        // enable OAuth2 login with GitLab
        .oauth2Login(oauth -> oauth
            // force this URL on success:
            .defaultSuccessUrl("http://localhost:4200/", true))
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("http://localhost:4200/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll());
    return http.build();
  }

}