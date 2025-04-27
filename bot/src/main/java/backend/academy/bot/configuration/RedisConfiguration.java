package backend.academy.bot.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

@Configuration
@EnableRedisRepositories
public class RedisConfiguration {
    @Bean
    public RedisTemplate<Long, Object> redisTemplateContext(
            RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<Long, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        ObjectMapper contextObjectMapper = objectMapper.copy().setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(contextObjectMapper));
        return template;
    }
}
