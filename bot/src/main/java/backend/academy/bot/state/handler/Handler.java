package backend.academy.bot.state.handler;

import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.State;

public interface Handler {
    boolean handle(HandlerContext context);

    State nextState();
}
