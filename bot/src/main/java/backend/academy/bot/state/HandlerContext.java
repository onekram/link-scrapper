package backend.academy.bot.state;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

public record HandlerContext(Message message, CallbackQuery callbackQuery, TelegramBot bot, State state) {
    public HandlerContext(Message message, TelegramBot bot, State state) {
        this(message, null, bot, state);
    }

    public HandlerContext(Update update, TelegramBot bot, State state) {
        this(update.message(), update.callbackQuery(), bot, state);
    }

    public long getChatId() {
        if (isCallbackQuery()) {
            return callbackQuery().from().id();
        }
        return message().chat().id();
    }

    public boolean isCallbackQuery() {
        return callbackQuery() != null;
    }
}
