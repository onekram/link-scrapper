package backend.academy.bot.state.filter;

import backend.academy.bot.repository.user.UserRepository;
import backend.academy.bot.state.HandlerContext;
import lombok.RequiredArgsConstructor;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class PresentUserFilter implements Predicate<HandlerContext> {
    private final UserRepository userRepository;

    @Override
    public boolean test(HandlerContext context) {
        return userRepository.exist(context.message().text());
    }
}
