package io.github.arthurmadalena.kallopolliapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KallopolliapiApplication {

	public static void main(String[] args) {
		SpringApplication.run(KallopolliapiApplication.class, args);
	}
}