package com.futebol.futebol_analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FutebolAnalyticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FutebolAnalyticsApplication.class, args);
	}

}
