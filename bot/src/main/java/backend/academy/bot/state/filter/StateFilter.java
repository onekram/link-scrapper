package backend.academy.bot.state.filter;

import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.State;
import lombok.RequiredArgsConstructor;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class StateFilter implements Predicate<HandlerContext> {
    private final State requiredState;

    @Override
    public boolean test(HandlerContext context) {
        return context.state().equals(requiredState);
    }
}
