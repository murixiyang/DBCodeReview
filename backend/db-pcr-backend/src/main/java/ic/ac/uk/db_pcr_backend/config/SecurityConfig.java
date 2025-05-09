package ic.ac.uk.db_pcr_backend.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // 1) Enable CORS support in the security filter chain
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())

        // 2) Your URL rules
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/hooks/gitlab").permitAll()
            .requestMatchers("/api/**").authenticated()
            .anyRequest().permitAll())

        // 3) For any unauthenticated /api/** request, return 401 instead of redirect
        .exceptionHandling(ex -> ex
            .defaultAuthenticationEntryPointFor(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                new AntPathRequestMatcher("/api/**")))

        // 4) OAuth2 login for everything else
        .oauth2Login(oauth -> oauth
            .defaultSuccessUrl("http://localhost:4200/", true))

        // 5) Logout stays the same
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("http://localhost:4200/")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll());

    return http.build();
  }

  // Wire in your CORS rules for all endpoints, including /oauth2/*
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:4200"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  // Map GitLab access levels to Spring Security roles
  @Bean
  public GrantedAuthoritiesMapper gitlabAuthoritiesMapper() {
    return authorities -> authorities.stream()
        .map(granted -> {
          if (granted.getAuthority().contains("access_level=Maintainer")) {
            return new SimpleGrantedAuthority("ROLE_MAINTAINER");
          }
          return new SimpleGrantedAuthority("ROLE_STUDENT");
        })
        .collect(Collectors.toList());
  }

}