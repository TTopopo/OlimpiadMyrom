package com.olimpiada;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.olimpiada")
public class OlimpiadMyromApplication {
    public static void main(String[] args) {
        SpringApplication.run(OlimpiadMyromApplication.class, args);
    }
} 