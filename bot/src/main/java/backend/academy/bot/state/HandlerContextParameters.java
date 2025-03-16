package backend.academy.bot.state;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class HandlerContextParameters {
    private final Map<String, Object> map = new HashMap<>();

    public void setParameter(String key, Object parameter) {
        map.put(key, parameter);
    }

    public Object getParameter(String key) {
        return map.get(key);
    }

    public <T> T getParameter(String key, Class<T> type) {
        Object value = map.get(key);
        if (type.isInstance(value)) {
            return type.cast(value);
        } else {
            throw new IllegalArgumentException("Value " + value + " for key " + key + " cannot be cast to " + type);
        }
    }

    public void clearParameter(String key) {
        map.remove(key);
    }
}
