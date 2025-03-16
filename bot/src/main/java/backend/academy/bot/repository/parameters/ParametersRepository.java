package backend.academy.bot.repository.parameters;

import backend.academy.bot.state.HandlerContextParameters;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ParametersRepository {
    private final Map<Long, ParametersRecord> map = new ConcurrentHashMap<>();

    public Optional<HandlerContextParameters> getContextParameters(Long key) {
        return Optional.ofNullable(map.get(key)).map(ParametersRecord::getParameters);
    }

    public void save(Long key, HandlerContextParameters parameters) {
        map.put(key, new ParametersRecord(parameters));
    }
}
