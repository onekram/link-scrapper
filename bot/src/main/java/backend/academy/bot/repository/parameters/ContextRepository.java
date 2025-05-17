package backend.academy.bot.repository.parameters;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ContextRepository {
    private final HashOperations<Long, Class<?>, String> hashOperations;
    private final ObjectMapper objectMapper;

    public ContextRepository(RedisTemplate<Long, Object> redisTemplate, ObjectMapper objectMapper) {
        hashOperations = redisTemplate.opsForHash();
        this.objectMapper = objectMapper.copy().setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }

    public void setContext(Long chatId, Object context) {
        try {
            hashOperations.put(chatId, context.getClass(), objectMapper.writeValueAsString(context));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(
                    "Fail to serialize object: %s for chatId: %s".formatted(context.getClass(), chatId), e);
        }
    }

    public <T> Optional<T> getContext(Long chatId, Class<T> token) {
        return Optional.ofNullable(hashOperations.get(chatId, token)).map(s -> {
            try {
                return objectMapper.readValue(s, token);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Fail to deserialize object: %s for chatId: %s".formatted(token, chatId), e);
            }
        });
    }

    public void deleteContext(Long chatId, Class<?> token) {
        hashOperations.delete(chatId, token);
    }
}
