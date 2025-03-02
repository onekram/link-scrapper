package backend.academy.bot.configuration;

import backend.academy.bot.BotConfig;
import backend.academy.bot.repository.user.UserRecord;
import com.pengrad.telegrambot.TelegramBot;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {
    private final BotConfig botConfig;

    @Bean
    public TelegramBot telegramBot() {
        return new TelegramBot(botConfig.telegramToken());
    }
}
