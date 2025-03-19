package backend.academy.bot.state.handler;

import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.State;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.springframework.beans.factory.annotation.Autowired;

@Builder
public class MessageHandler implements Handler {
    @Singular("withFilter")
    private final List<Predicate<HandlerContext>> filters;
    @Getter
    @Builder.Default
    private State nextState = State.MENU;
    private final String message;
    private final Function<HandlerContext, SendMessage> method;
    private Keyboard keyboard;
    private boolean menuButton;

    @Autowired
    private ResourceBundle resourceBundle;

    @Override
    public boolean handle(HandlerContext context) {
        if (filters.stream().allMatch(filter -> filter.test(context))) {
            process(context);
            return true;
        }
        return false;
    }

    private void process(HandlerContext context) {
        SendMessage response = method == null
            ? new SendMessage(context.message().chat().id(), message)
            : method.apply(context);
        response = addKeyboard(response);
        context.bot().execute(response);
    }

    private SendMessage addKeyboard(SendMessage response) {
        switch (keyboard) {
            case null -> {
                if (menuButton) {
                    return response.replyMarkup(new ReplyKeyboardMarkup(resourceBundle.getString("menu.message")));
                }
                return response;
            }
            case ReplyKeyboardRemove ignored -> {
                if (menuButton) {
                    return response.replyMarkup(new ReplyKeyboardMarkup(resourceBundle.getString("menu.message")));
                }
                return response.replyMarkup(keyboard);
            }
            case ReplyKeyboardMarkup replyKeyboardMarkup when menuButton -> {
                return response.replyMarkup(replyKeyboardMarkup.addRow(resourceBundle.getString("menu.message")));
            }
            default -> {
                return response.replyMarkup(keyboard);
            }
        }
    }
}
