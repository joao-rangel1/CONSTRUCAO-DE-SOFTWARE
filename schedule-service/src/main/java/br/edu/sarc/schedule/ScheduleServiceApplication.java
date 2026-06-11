package br.edu.sarc.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ScheduleServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(ScheduleServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ScheduleServiceApplication.class, args);
    }

    @Bean
    ApplicationRunner onStartup() {
        return args -> log.info("schedule-service iniciado com sucesso");
    }
}
