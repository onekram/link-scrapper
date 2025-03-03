package backend.academy.scrapper.service;

import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.repository.ChatRecord;
import backend.academy.scrapper.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

    public void register(Long id) {
        chatRepository.saveUser(id);
    }

    public void unRegister(Long id) {
        ChatRecord record = chatRepository.removeUser(id);
        if (record == null) {
            throw new NotFoundException(String.format("Не существует чата с ID: %s", id));
        }
    }
}
