package backend.academy.bot.configuration;

import backend.academy.bot.BotConfig;
import com.pengrad.telegrambot.TelegramBot;
import java.util.ResourceBundle;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {
    private final BotConfig botConfig;

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot.Builder(botConfig.telegramToken())
            .apiUrl(botConfig.telegramUrl() + "/bot")
            .build();
    }

    @Bean
    public ResourceBundle resourceBundle() {
        return ResourceBundle.getBundle("message_en");
    }
}
