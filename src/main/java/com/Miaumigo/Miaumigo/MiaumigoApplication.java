package com.Miaumigo.Miaumigo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MiaumigoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiaumigoApplication.class, args);
	}

}
