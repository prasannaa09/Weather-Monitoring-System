package com.example.weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class WeatherApplication {

	public static void main(String[] args) {
		SpringApplication.run(WeatherApplication.class, args);
	}

	// Enable CORS globally for all endpoints
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				// Allow requests from your frontend (adjust the port as needed)
				registry.addMapping("/**")
						.allowedOrigins("http://localhost:5500")  // Allow only your frontend to make requests
						.allowedMethods("GET", "POST", "PUT", "DELETE")  // Allow common HTTP methods
						.allowedHeaders("*")  // Allow any headers
						.allowCredentials(true);  // Allow credentials (e.g., cookies, authorization headers)
			}
		};
	}
}
