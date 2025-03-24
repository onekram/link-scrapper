package backend.academy.bot.service;

import backend.academy.bot.util.LocalTunnelUtil;
import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.DeleteWebhook;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;
import jakarta.annotation.PostConstruct;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BotService {
    private final TelegramBot telegramBot;
    private final UpdateService updateService;

    @Value("${server.port}")
    private Integer port;

    @Value("${features.webhook.enabled}")
    private boolean webhookEnabled;

    @PostConstruct
    public void initBot() {
        if (webhookEnabled) {
            try {
                useWebhook();
            } catch (WebhookException e) {
                useUpdateListener();
            }
        } else {
            useUpdateListener();
        }
    }

    private void useWebhook() throws WebhookException {
        BaseResponse response;
        try {
            String url = LocalTunnelUtil.startLocalTunnel(port);
            response = telegramBot.execute(new SetWebhook().url(url));
            log.info("Set webhook response: {}", response);
        } catch (Exception e) {
            throw new WebhookException("Error while setting webhook");
        }
        if (!response.isOk()) {
            throw new WebhookException("Webhook is not set");
        }
    }

    private void useUpdateListener() {
        BaseResponse response = telegramBot.execute(new DeleteWebhook());
        log.info("Delete webhook response: {}", response);
        telegramBot.setUpdatesListener(updatesListener(), exceptionHandler(), getUpdates());
    }

    private GetUpdates getUpdates() {
        return new GetUpdates().limit(100).timeout(10);
    }

    private UpdatesListener updatesListener() {
        return updates -> {
            updates.forEach(updateService::updateProcess);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        };
    }

    private ExceptionHandler exceptionHandler() {
        return e -> {
            if (e.response() != null) {
                log.error(
                        "Telegram API not responding: {} - {}",
                        e.response().errorCode(),
                        e.response().description());
            } else {
                String message = Stream.of(e.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n"));
                log.error(message);
            }
        };
    }
}
