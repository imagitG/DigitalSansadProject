package com.digitalSansad.sow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class SowServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(SowServiceApplication.class);

	public static void main(String[] args) {

		logger.info("APP STARTED CHECKPOINT 🚀");

		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		logger.info("Dotenv file loaded");

		dotenv.entries().forEach(e -> {
			System.setProperty(e.getKey(), e.getValue());
			logger.debug("Set property: {} from .env", e.getKey());
		});
		logger.info("Loaded {} environment properties from .env", dotenv.entries().size());

		SpringApplication.run(SowServiceApplication.class, args);
	}

}
