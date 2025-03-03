package backend.academy.bot.service;

import backend.academy.bot.repository.state.StateRepository;
import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.Router;
import backend.academy.bot.state.State;
import com.pengrad.telegrambot.ExceptionHandler;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BotService {
    private final TelegramBot telegramBot;
    private final Router router;
    private final StateRepository stateRepository;

    @PostConstruct
    public void initBot() {
        telegramBot.setUpdatesListener(updatesListener(), exceptionHandler()); // TODO Use webHook instead
    }

    private GetUpdates getUpdates() {
        return new GetUpdates().limit(100).timeout(10);
    }

    private UpdatesListener updatesListener() {
        return updates -> {
            for (Update update : updates) {
                if (update.message() != null) {
                    Message message = update.message();
                    long chatId = message.chat().id();

                    try {
                        State currentState = stateRepository.getCurrentState(chatId);
                        log.info("Message chatId: {}, current state: {}, text: {}", chatId, currentState, message.text());

                        State nextState = router.process(new HandlerContext(
                            message,
                            telegramBot,
                            currentState
                        ));
                        log.info("Move to state: {}", nextState);
                        stateRepository.saveState(chatId, nextState);
                    } catch (Exception e) {
                        log.error("Exception while routing occurred", e);
                        telegramBot.execute(new SendMessage(chatId, "\uD83D\uDEA8 Error occurs... Try later \uD83D\uDD27"));
                        stateRepository.saveState(chatId, State.MENU);
                    }
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        };
    }

    private ExceptionHandler exceptionHandler() {
        return e -> {
            if (e.response() != null) {
                log.error("Telegram API not responding: {} - {}", e.response().errorCode(), e.response().description());
            } else {
                String message = Stream.of(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .collect(Collectors.joining("\n"));
                log.error(message);
            }
        };
    }
}
