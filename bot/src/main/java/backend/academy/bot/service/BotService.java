package backend.academy.bot.service;

import backend.academy.bot.repository.state.StateRepository;
import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.Router;
import backend.academy.bot.state.State;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import jakarta.annotation.PostConstruct;
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
        telegramBot.setUpdatesListener(updates -> {
            try {
                for (Update update : updates) {
                    if (update.message() != null) {
                        Message message = update.message();

                        long chatId = message.chat().id();
                        State currentState = stateRepository.getCurrentState(chatId);
                        log.info("Message chatId: {}, current state: {}, text: {}", chatId, currentState, message.text());

                        State nextState = router.process(new HandlerContext(
                            message,
                            telegramBot,
                            currentState
                        ));
                        log.info("Move to state: {}", nextState);
                        stateRepository.saveState(chatId, nextState);
                    }
                }
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            } catch (Exception e) {
                log.error("Exception while routing occurred", e);
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        });
    }
}
