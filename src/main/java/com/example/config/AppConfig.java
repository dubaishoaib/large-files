package com.example.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = {"com.example.repo"})
@EntityScan(basePackages = {"com.example.domain"})
@EnableTransactionManagement
@ComponentScan(basePackages = {"com.example.service"})
public class AppConfig {
}
