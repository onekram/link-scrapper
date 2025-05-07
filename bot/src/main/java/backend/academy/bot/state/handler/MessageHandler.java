package backend.academy.bot.state.handler;

import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.State;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.AbstractSendRequest;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.Singular;
import org.springframework.beans.factory.annotation.Autowired;

public class MessageHandler implements Handler {
    private final List<Predicate<HandlerContext>> filters;
    private State nextState;
    private final String message;
    private final Function<HandlerContext, BaseRequest<?, ?>> method;
    private Keyboard keyboard;
    private final boolean menuButton;
    private final boolean callback;

    @Autowired
    private ResourceBundle resourceBundle;

    @Builder
    private MessageHandler(
            @Singular("withFilter") List<Predicate<HandlerContext>> filters,
            State nextState,
            String message,
            Function<HandlerContext, BaseRequest<?, ?>> method,
            Keyboard keyboard,
            boolean menuButton,
            boolean callback) {
        this.filters = filters;
        this.nextState = nextState;
        this.message = message;
        this.method = method;
        this.keyboard = keyboard;
        this.menuButton = menuButton;
        this.callback = callback;
    }

    @PostConstruct
    private void postConstruct() {
        updateKeyboard();
    }

    @Override
    public boolean handle(HandlerContext context) {
        if (callback != context.isCallbackQuery()) {
            return false;
        }
        if (filters.stream().allMatch(filter -> filter.test(context))) {
            process(context);
            if (nextState == null) {
                nextState = context.state();
            }
            return true;
        }
        return false;
    }

    @Override
    public State nextState() {
        return nextState == null ? State.MENU : nextState;
    }

    private void process(HandlerContext context) {
        BaseRequest<?, ?> request =
                method == null ? new SendMessage(context.getChatId(), message) : method.apply(context);
        if (keyboard != null && request instanceof AbstractSendRequest<?> sendRequest) {
            sendRequest.replyMarkup(keyboard);
        }
        context.bot().execute(request);
    }

    private void updateKeyboard() {
        if (menuButton) {
            switch (keyboard) {
                case null -> keyboard =
                        new ReplyKeyboardMarkup(resourceBundle.getString("menu.message")).resizeKeyboard(true);
                case ReplyKeyboardRemove ignored -> keyboard =
                        new ReplyKeyboardMarkup(resourceBundle.getString("menu.message")).resizeKeyboard(true);
                case ReplyKeyboardMarkup replyKeyboardMarkup -> replyKeyboardMarkup.addRow(
                        resourceBundle.getString("menu.message"));
                default -> {}
            }
        }
    }

    public static class MessageHandlerBuilder {
        public MessageHandlerBuilder callback() {
            this.callback = true;
            return this;
        }

        public MessageHandlerBuilder menuButton() {
            this.menuButton = true;
            return this;
        }
    }
}
