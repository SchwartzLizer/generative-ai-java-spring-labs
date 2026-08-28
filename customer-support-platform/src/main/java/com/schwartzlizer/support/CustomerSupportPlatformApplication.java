package com.schwartzlizer.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CustomerSupportPlatformApplication {
    public static void main(String[] args) { SpringApplication.run(CustomerSupportPlatformApplication.class, args); }
}
