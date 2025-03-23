package com.flagcamp.TripPlanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TripPlannerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripPlannerApplication.class, args);
	}

}
