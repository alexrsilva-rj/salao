package com.salao.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.salao.api",
        "com.salao.security",
        "com.salao.cliente",
        "com.salao.agendamento",
        "com.salao.financeiro"
})
@EnableJpaRepositories(basePackages = {
        "com.salao.security",
        "com.salao.cliente",
        "com.salao.agendamento",
        "com.salao.financeiro"
})
@EntityScan(basePackages = {
        "com.salao.security",
        "com.salao.cliente",
        "com.salao.agendamento",
        "com.salao.financeiro"
})
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
