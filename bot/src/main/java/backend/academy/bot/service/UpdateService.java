package backend.academy.bot.service;

import backend.academy.bot.repository.state.StateRepository;
import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.Router;
import backend.academy.bot.state.State;
import backend.academy.model.LinkUpdate;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.io.IOException;
import java.util.ResourceBundle;
import com.pengrad.telegrambot.response.SendResponse;
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
            log.info("Message chatId: {}, move to state: {}", chatId, nextState);
            stateRepository.saveState(chatId, nextState);
        } catch (Exception e) {
            log.error("Exception while routing occurred", e);
            telegramBot.execute(new SendMessage(chatId, resourceBundle.getString("error.message")));
            stateRepository.saveState(chatId, State.MENU);
        }
    }

    public void updateProcess(LinkUpdate linkUpdate) {
        linkUpdate
            .getTgChatIds()
            .forEach(chatId -> telegramBot.execute(
                new SendMessage(
                    chatId,
                    resourceBundle.getString("update.format.message").formatted(linkUpdate.getDescription(), linkUpdate.getUrl())),
                new Callback<SendMessage, SendResponse>() {
                    @Override
                    public void onResponse(SendMessage request, SendResponse response) {
                        log.info("Message sent: {} with response: {}", request, response);
                    }

                    @Override
                    public void onFailure(SendMessage request, IOException e) {
                        log.error("Message sent: {} with error", request, e);
                    }
                }));
    }
}
