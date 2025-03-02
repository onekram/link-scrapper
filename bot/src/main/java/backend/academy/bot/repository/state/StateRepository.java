package backend.academy.bot.repository.state;

import backend.academy.bot.state.State;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class StateRepository {
    private final Map<Long, StateRecord> db = new ConcurrentHashMap<>();

    public void saveState(Long id, State state) {
        db.merge(id,
            new StateRecord(state, State.MENU),
            (oldValue, newValue) -> new StateRecord(newValue.getCurrent(), oldValue.getCurrent()));
    }

    public State getCurrentState(Long id) {
        return getStateRecord(id).getCurrent();
    }

    public State getPreviousState(Long id) {
        return getStateRecord(id).getPrevious();
    }

    private StateRecord getStateRecord(Long id) {
        return db.computeIfAbsent(id, key -> new StateRecord(State.START, State.MENU));
    }
}
