package com.core.cashin.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.core.cashin")
@EnableJpaRepositories(basePackages = "com.core.cashin.routing.repository")
@EntityScan(basePackages = "com.core.cashin.routing.entity")
public class App {
    public static void main(String[] args) {
        System.setProperty("spring.config.location", "config/");
        SpringApplication.run(App.class, args);
    }
}
