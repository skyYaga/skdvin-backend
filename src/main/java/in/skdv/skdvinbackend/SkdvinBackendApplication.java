package in.skdv.skdvinbackend;

import in.skdv.skdvinbackend.config.ApplicationConfig;
import in.skdv.skdvinbackend.config.EmailConfig;
import in.skdv.skdvinbackend.config.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Configuration
@Import({ApplicationConfig.class, SecurityConfig.class, EmailConfig.class})
public class SkdvinBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkdvinBackendApplication.class, args);
	}
}
