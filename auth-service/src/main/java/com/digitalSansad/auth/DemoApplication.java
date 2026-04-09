package com.digitalSansad.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		System.out.println("APP STARTED CHECKPOINT 🚀");

		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

		dotenv.entries().forEach(e -> {
			System.setProperty(e.getKey(), e.getValue());
		});

		SpringApplication.run(DemoApplication.class, args);
	}

}
