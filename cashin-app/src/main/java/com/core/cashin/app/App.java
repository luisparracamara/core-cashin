package com.core.cashin.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Hello world!
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.core.cashin")
@EnableJpaRepositories(basePackages = "com.core.cashin")
@EntityScan(basePackages = "com.core.cashin.commons.entity")
@EnableScheduling
public class App {
    public static void main(String[] args) {
        System.setProperty("spring.config.location", "config/");
        SpringApplication.run(App.class, args);
    }
}
