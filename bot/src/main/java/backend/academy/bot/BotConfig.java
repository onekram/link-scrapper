package backend.academy.bot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
public record BotConfig(
        @NotBlank(message = "Telegram token must not be blank") String telegramToken,
        @NotBlank(message = "Scrapper URL must not be blank")
                @Pattern(regexp = "^(http|https)://.*", message = "Base URL must start with http:// or https://")
                String scrapperUrl) {}
