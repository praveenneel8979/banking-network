package com.banking.frouddetectionservice;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.banking.frouddetectionservice.client")
public class FroudDetectionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FroudDetectionServiceApplication.class, args);
	}

}
