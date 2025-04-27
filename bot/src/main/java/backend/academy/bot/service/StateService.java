package backend.academy.bot.service;

import backend.academy.bot.repository.state.StateEntity;
import backend.academy.bot.repository.state.StateRepository;
import backend.academy.bot.state.State;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StateService {
    private static final State INIT_STATE = State.START;
    private final StateRepository stateRepository;

    public State getState(Long chatId) {
        return stateRepository
            .findById(chatId)
            .map(StateEntity::state)
            .orElse(INIT_STATE);
    }

    public void setState(Long chatId, State newState) {
        stateRepository.save(new StateEntity(chatId, newState));
    }
}
