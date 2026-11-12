package com.example.Quanlysanpham;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class QuanlysanphamApplication {

    static void main(String[] args) {
        SpringApplication.run(QuanlysanphamApplication.class, args);
    }

}
