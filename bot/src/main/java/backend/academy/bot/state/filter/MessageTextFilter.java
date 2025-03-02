package backend.academy.bot.state.filter;

import backend.academy.bot.state.HandlerContext;
import lombok.RequiredArgsConstructor;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class MessageTextFilter implements Predicate<HandlerContext> {
    private final String requiredText;

    @Override
    public boolean test(HandlerContext context) {
        return context.message().text().equalsIgnoreCase(requiredText);
    }
}
