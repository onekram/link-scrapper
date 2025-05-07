package backend.academy.bot.state.filter;

import backend.academy.bot.state.HandlerContext;
import lombok.RequiredArgsConstructor;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class CallbackFilter implements Predicate<HandlerContext> {
    private final String expectedPrefix;
    @Override
    public boolean test(HandlerContext handlerContext) {
        if (!handlerContext.isCallbackQuery()) return false;
        return handlerContext.callbackQuery().data().startsWith(expectedPrefix + ":");
    }
}
