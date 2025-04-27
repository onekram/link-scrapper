package backend.academy.bot.service;

import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.Router;
import backend.academy.bot.state.State;
import backend.academy.model.LinkUpdate;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import java.io.IOException;
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
    private final StateService stateService;
    private final ResourceBundle resourceBundle;

    public void updateProcess(Update update) {
        Message message = update.message();
        if (message == null) {
            return;
        }
        long chatId = message.chat().id();
        try {
            State currentState = stateService.getState(chatId);
            log.info("Message chatId: {}, current state: {}, text: {}", chatId, currentState, message.text());

            State nextState = router.process(new HandlerContext(message, telegramBot, currentState));
            log.info("Message chatId: {}, move to state: {}", chatId, nextState);
            stateService.setState(chatId, nextState);
        } catch (Exception e) {
            log.error("Exception while routing occurred", e);
            telegramBot.execute(new SendMessage(chatId, resourceBundle.getString("error.message")));
            stateService.setState(chatId, State.MENU);
        }
    }

    public void updateProcess(LinkUpdate linkUpdate) {
        linkUpdate
                .tgChatIds()
                .forEach(chatId -> telegramBot.execute(
                        new SendMessage(
                                chatId,
                                resourceBundle
                                        .getString("update.format.message")
                                        .formatted(linkUpdate.description(), linkUpdate.url())),
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
