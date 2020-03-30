package com.company.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
@EntityScan( basePackages = {"org.jbpm.process.audit"} )
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}