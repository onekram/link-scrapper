package backend.academy.scrapper;

import org.springframework.boot.SpringApplication;

public class TestApplication {

    public static void main(String[] args) {
        System.setProperty("spring.liquibase.change-log", "file:migrations/master.yml");

        SpringApplication.from(ScrapperApplication::main)
                .with(TestcontainersConfiguration.class)
                .withAdditionalProfiles("test")
                .run(args);
    }
}
