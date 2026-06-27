package com.itshere.backend;

import com.itshere.backend.config.ItsHereProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ItsHereProperties.class)
public class ItsHereBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItsHereBackendApplication.class, args);
    }
}
