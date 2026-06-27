package br.edu.sarc.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ResourceServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(ResourceServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ResourceServiceApplication.class, args);
    }

    @Bean
    ApplicationRunner onStartup() {
        return args -> log.info("resource-service iniciado com sucesso");
    }
}
