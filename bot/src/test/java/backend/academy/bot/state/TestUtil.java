package backend.academy.bot.state;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import java.lang.reflect.Field;

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
}
