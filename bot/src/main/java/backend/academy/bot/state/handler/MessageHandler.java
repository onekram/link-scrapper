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
import jakarta.annotation.PostConstruct;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.springframework.beans.factory.annotation.Autowired;

public class MessageHandler implements Handler {
    private final List<Predicate<HandlerContext>> filters;
    @Getter
    private State nextState = State.MENU;
    private final String message;
    private final Function<HandlerContext, SendMessage> method;
    private Keyboard keyboard;
    private final boolean menuButton;

    @Autowired
    private ResourceBundle resourceBundle;

    @Builder
    private MessageHandler(@Singular("withFilter") List<Predicate<HandlerContext>> filters,
                           State nextState, String message,
                           Function<HandlerContext, SendMessage> method,
                           Keyboard keyboard,
                           boolean menuButton) {
        this.filters = filters;
        this.nextState = nextState == null ? this.nextState : nextState;
        this.message = message;
        this.method = method;
        this.keyboard = keyboard;
        this.menuButton = menuButton;
    }

    @PostConstruct
    private void postConstruct() {
        updateKeyboard();
    }

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
        if (keyboard != null) {
            response.replyMarkup(keyboard);
        }
        context.bot().execute(response);
    }

    private void updateKeyboard() {
        switch (keyboard) {
            case null -> {
                if (menuButton) {
                    keyboard = new ReplyKeyboardMarkup(resourceBundle.getString("menu.message"));
                }
            }
            case ReplyKeyboardRemove ignored when menuButton ->
                keyboard = new ReplyKeyboardMarkup(resourceBundle.getString("menu.message"));
            case ReplyKeyboardMarkup replyKeyboardMarkup when menuButton ->
                replyKeyboardMarkup.addRow(resourceBundle.getString("menu.message"));
            default -> {}
        }
    }
}
