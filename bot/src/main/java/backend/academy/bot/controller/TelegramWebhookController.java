package backend.academy.bot.controller;

import backend.academy.bot.service.UpdateService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.utility.BotUtils;
import java.io.BufferedReader;
import java.io.Reader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "features.webhook.enabled", havingValue = "true")
@RestController
public class TelegramWebhookController {
    private final UpdateService updateService;

    @PostMapping("/")
    public void handleUpdates(Reader reader) {
        Update update = BotUtils.parseUpdate(new BufferedReader(reader));
        log.info("Received update by webhook: {}", update.toString());
        updateService.updateProcess(update);
    }
}
