package org.telegram.expensesbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.telegram.telegrambots.ApiContextInitializer;

@EnableJpaRepositories("org.telegram.expensesbot.repository")
@EntityScan("org.telegram.expensesbot.model")
@SpringBootApplication
public class TelegramBotApplication {

    public static void main(String[] args) {
        ApiContextInitializer.init();
        SpringApplication.run(TelegramBotApplication.class, args);
    }
}
