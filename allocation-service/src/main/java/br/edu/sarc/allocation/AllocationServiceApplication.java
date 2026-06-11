package br.edu.sarc.allocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AllocationServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(AllocationServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AllocationServiceApplication.class, args);
    }

    @Bean
    ApplicationRunner onStartup() {
        return args -> log.info("allocation-service iniciado com sucesso");
    }
}
