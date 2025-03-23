package backend.academy.bot.util;

import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.State;
import backend.academy.model.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.message.MaybeInaccessibleMessage;
import com.pengrad.telegrambot.request.BaseRequest;
import java.lang.reflect.Field;
import java.util.List;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtil {
    public static HandlerContext generateHandlerContext() {
        return new HandlerContext(new Message(), new TelegramBot("123"), State.START);
    }

    @SneakyThrows
    public static HandlerContext generateHandlerContext(String text) {
        Message message = new Message();
        Field field = Message.class.getDeclaredField("text");
        field.setAccessible(true);
        field.set(message, text);

        return new HandlerContext(message, new TelegramBot("123"), State.START);
    }

    public static HandlerContext generateHandlerContext(State state) {
        return new HandlerContext(new Message(), new TelegramBot("123"), state);
    }

    @SneakyThrows
    public static Message generateMessage(String text, Long chatId) {
        Message message = new Message();
        Field fieldText = Message.class.getDeclaredField("text");
        fieldText.setAccessible(true);
        fieldText.set(message, text);

        Chat chat = new Chat();
        Field fieldId = Chat.class.getDeclaredField("id");
        fieldId.setAccessible(true);
        fieldId.set(chat, chatId);

        Field fieldChat = MaybeInaccessibleMessage.class.getDeclaredField("chat");
        fieldChat.setAccessible(true);
        fieldChat.set(message, chat);
        return message;
    }

    @SneakyThrows
    public static Update generateUpdate(Message message) {
        Update update = new Update();
        Field field = Update.class.getDeclaredField("message");
        field.setAccessible(true);
        field.set(update, message);

        return update;
    }

    public static LinkUpdate generateLinkUpdate(Long... ids) {
        return new LinkUpdate(42L, "url", "description", List.of(ids));
    }

    public static Long getId(BaseRequest<?, ?> sendMessage) {
        return (Long) sendMessage.getParameters().get("chat_id");
    }

    public static String getText(BaseRequest<?, ?> sendMessage) {
        return (String) sendMessage.getParameters().get("text");
    }
}
