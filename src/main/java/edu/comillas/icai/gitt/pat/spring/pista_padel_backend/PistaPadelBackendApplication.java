package edu.comillas.icai.gitt.pat.spring.pista_padel_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PistaPadelBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PistaPadelBackendApplication.class, args);
	}

}
