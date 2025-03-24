package backend.academy.bot.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.State;
import backend.academy.bot.state.handler.MessageHandler;
import backend.academy.model.LinkUpdate;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.request.BaseRequest;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
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

    public static Keyboard getKeyboardFromHandler(MessageHandler messageHandler) {
        return (Keyboard) ReflectionTestUtils.getField(messageHandler, "keyboard");
    }

    public static void assertContainsButton(MessageHandler messageHandler, String... texts) {
        Keyboard keyboard = getKeyboardFromHandler(messageHandler);
        assertNotNull(keyboard);
        assertContainsButton(keyboard, texts);
    }

    public static void assertContainsButton(Keyboard keyboard, String... texts) {
        assertContainOrNot(keyboard, stream -> stream::anyMatch, texts);
    }

    public static void assertDoesntContainButton(Keyboard keyboard, String... texts) {
        assertContainOrNot(keyboard, stream -> stream::noneMatch, texts);
    }

    @SuppressWarnings("unchecked")
    private static void assertContainOrNot(
            Keyboard keyboard,
            Function<Stream<KeyboardButton>, Predicate<Predicate<KeyboardButton>>> matchFunction,
            String... texts) {
        List<List<KeyboardButton>> buttonsRows =
                (List<List<KeyboardButton>>) ReflectionTestUtils.getField(keyboard, "keyboard");
        assertNotNull(buttonsRows);

        for (String text : texts) {
            assertTrue(matchFunction
                    .apply(buttonsRows.stream().flatMap(List::stream))
                    .test(keyboardButton -> {
                        String buttonText = (String) ReflectionTestUtils.getField(keyboardButton, "text");
                        assertNotNull(buttonText);
                        return buttonText.contains(text);
                    }));
        }
    }
}
