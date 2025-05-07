package backend.academy.bot.service;

import static backend.academy.bot.util.BotUtil.getChatIdFromUpdate;
import static backend.academy.bot.util.LogUtil.logReceivedUpdate;

import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.Router;
import backend.academy.bot.state.State;
import backend.academy.model.LinkUpdate;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.LinkPreviewOptions;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
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
        log.info("Update received from user {}", update);

        long chatId = getChatIdFromUpdate(update);
        try {
            State currentState = stateService.getState(chatId);
            logReceivedUpdate(update, currentState);
            State nextState = router.process(new HandlerContext(update, telegramBot, currentState));
            log.info("Message chatId: {}, move to state: {}", chatId, nextState);
            stateService.setState(chatId, nextState);
        } catch (Exception e) {
            log.error("Exception while routing occurred", e);
            telegramBot.execute(new SendMessage(chatId, resourceBundle.getString("error.message")));
            stateService.setState(chatId, State.MENU);
        }
    }

    public void updateProcess(LinkUpdate linkUpdate) {
        String updateMessage = resourceBundle
                .getString("update.format.message")
                .formatted(
                        linkUpdate.title(),
                        linkUpdate.resourceUrl(),
                        linkUpdate.description(),
                        linkUpdate.user(),
                        linkUpdate.userUrl());
        linkUpdate
                .tgChatIds()
                .forEach(chatId -> telegramBot.execute(
                        new SendMessage(chatId, updateMessage)
                                .parseMode(ParseMode.HTML)
                                .linkPreviewOptions(new LinkPreviewOptions().isDisabled(true))
                                .replyMarkup(new InlineKeyboardMarkup(
                                        new InlineKeyboardButton(resourceBundle.getString(
                                                        "link.to.update.inline.button.message"))
                                                .url(linkUpdate.updateUrl()),
                                        new InlineKeyboardButton(resourceBundle.getString(
                                                        "delete.subscription.inline.button.message"))
                                                .callbackData("delete_subscription:" + linkUpdate.resourceUrl()))),
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
