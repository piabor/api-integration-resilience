package com.example.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PaymentClientServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentClientServiceApplication.class, args);
    }
}
