package backend.academy.bot.repository.state;

import backend.academy.bot.state.State;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("chat_state")
public record StateEntity(
    @Id
    Long chatId,
    State state
) {
}

