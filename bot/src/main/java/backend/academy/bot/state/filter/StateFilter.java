package backend.academy.bot.state.filter;

import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.State;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StateFilter implements Predicate<HandlerContext> {
    private final State requiredState;

    @Override
    public boolean test(HandlerContext context) {
        return context.state().equals(requiredState);
    }
}
