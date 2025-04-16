package backend.academy.bot.repository.state;

import backend.academy.bot.state.State;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class StateRepository {
    private final Map<Long, StateRecord> db = new ConcurrentHashMap<>();

    public void saveState(Long id, State state) {
        db.put(id, new StateRecord(state));
    }

    public State getCurrentState(Long id) {
        return getStateRecord(id).getCurrent();
    }

    private StateRecord getStateRecord(Long id) {
        return db.computeIfAbsent(id, ignore -> new StateRecord(State.START));
    }
}
