package com.tawn.tawnht;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients
@EnableAsync
public class TawnhtApplication {
	public static void main(String[] args) {
		SpringApplication.run(TawnhtApplication.class, args);
	}
}
