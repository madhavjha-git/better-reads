package site.madhavjha.betterreadswebapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import site.madhavjha.betterreadswebapp.connection.DataStaxAstraProperties;

@EnableConfigurationProperties(DataStaxAstraProperties.class)
@SpringBootApplication
public class BetterReadsWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(BetterReadsWebApplication.class, args);
	}

}
