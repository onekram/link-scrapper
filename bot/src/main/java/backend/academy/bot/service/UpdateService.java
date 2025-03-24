package backend.academy.bot.service;

import backend.academy.bot.repository.state.StateRepository;
import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.Router;
import backend.academy.bot.state.State;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.ResourceBundle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateService {
    private final TelegramBot telegramBot;
    private final Router router;
    private final StateRepository stateRepository;
    private final ResourceBundle resourceBundle;

    public void updateProcess(Update update) {
        Message message = update.message();
        if (message == null) {
            return;
        }
        long chatId = message.chat().id();
        try {
            State currentState = stateRepository.getCurrentState(chatId);
            log.info("Message chatId: {}, current state: {}, text: {}", chatId, currentState, message.text());

            State nextState = router.process(new HandlerContext(message, telegramBot, currentState));
            log.info("Move to state: {}", nextState);
            stateRepository.saveState(chatId, nextState);
        } catch (Exception e) {
            log.error("Exception while routing occurred", e);
            telegramBot.execute(new SendMessage(chatId, resourceBundle.getString("error.message")));
            stateRepository.saveState(chatId, State.MENU);
        }
    }
}
