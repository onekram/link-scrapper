package backend.academy.bot.configuration;

import backend.academy.bot.BotConfig;
import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {
    @Autowired
    private BotConfig botConfig;

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(botConfig.telegramToken());
    }
}
