package com.tawn.tawnht;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class TawnhtApplication {
    public static void main(String[] args) {
        SpringApplication.run(TawnhtApplication.class, args);
    }
}
