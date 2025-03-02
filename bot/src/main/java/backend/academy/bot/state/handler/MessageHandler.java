package backend.academy.bot.state.handler;

import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.State;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Builder
public class MessageHandler implements Handler {
    @Singular("withFilter")
    private final List<Predicate<HandlerContext>> filters;
    @Getter
    @Builder.Default
    private State nextState = State.MENU;
    private final String message;
    private final Function<HandlerContext, SendMessage> method;
    private final ReplyKeyboardMarkup keyboard;


    @Override
    public boolean handle(HandlerContext context) {
       if (filters.stream().allMatch(filter -> filter.test(context))) {
           process(context);
           return true;
       }
       return false;
    }

    private void process(HandlerContext context) {
        SendMessage response;
        if (method == null) {
            response = new SendMessage(
                context.message().chat().id(),
                message
            );
            if (keyboard != null) {
                response = response.replyMarkup(keyboard);
            }
        } else {
            response = method.apply(context);
        }
        context.bot().execute(response);
    }
}
