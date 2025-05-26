package ic.ac.uk.db_pcr_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200", "http://20.77.48.174:4200/")
                .allowCredentials(true);
        registry.addMapping("/oauth2/**")
                .allowedOrigins("http://localhost:4200", "http://20.77.48.174:4200/")
                .allowCredentials(true);
    }
}
