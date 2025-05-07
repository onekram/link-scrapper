package backend.academy.bot.util;

import backend.academy.bot.state.State;
import com.pengrad.telegrambot.model.Update;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import static backend.academy.bot.util.BotUtil.getChatIdFromUpdate;

@Slf4j
@UtilityClass
public class LogUtil {
    public static void logReceivedUpdate(Update update, State state) {
        long chatId = getChatIdFromUpdate(update);
        if (update.message() != null) {
            log.info("Message chatId: {}, current state: {}, text: {}", chatId, state, update.message().text());
        } else if (update.callbackQuery() != null) {
            log.info("Message chatId: {}, current state: {}, callback data: {}", chatId, state, update.callbackQuery().data());
        }
    }
}
