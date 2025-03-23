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
import java.sql.Ref;
import java.util.List;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.test.util.ReflectionTestUtils;

@UtilityClass
public class TestUtil {
    public static HandlerContext generateHandlerContext() {
        return new HandlerContext(new Message(), new TelegramBot("123"), State.START);
    }

    public static HandlerContext generateHandlerContext(String text) {
        Message message = generateMessage(text, 1L);
        return new HandlerContext(message, new TelegramBot("123"), State.START);
    }

    public static HandlerContext generateHandlerContext(State state) {
        return new HandlerContext(new Message(), new TelegramBot("123"), state);
    }

    @SneakyThrows
    public static Message generateMessage(String text, Long chatId) {
        Chat chat = new Chat();
        ReflectionTestUtils.setField(chat, "id", chatId);

        Message message = new Message();
        ReflectionTestUtils.setField(message, "text", text);
        ReflectionTestUtils.setField(message, "chat", chat);
        return message;
    }

    @SneakyThrows
    public static Update generateUpdate(Message message) {
        Update update = new Update();
        ReflectionTestUtils.setField(update, "message", message);
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
