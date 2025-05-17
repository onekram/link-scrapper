package backend.academy.bot.util;

import com.pengrad.telegrambot.model.Update;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BotUtil {
    public static long getChatIdFromUpdate(Update update) {
        if (update.callbackQuery() != null) {
            return update.callbackQuery().from().id();
        }
        return update.message().chat().id();
    }
}
