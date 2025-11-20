package com.reskill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ReskillApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReskillApplication.class, args);
	}

}
