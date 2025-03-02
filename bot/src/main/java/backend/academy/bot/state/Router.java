package backend.academy.bot.state;

import backend.academy.bot.state.handler.Handler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Router {
    private final List<Handler> handlers;

    public State process(HandlerContext context) {
        return handlers.stream()
            .filter(handler -> handler.handle(context))
            .map(Handler::nextState)
            .findFirst()
            .orElse(State.MENU);
    }
}
