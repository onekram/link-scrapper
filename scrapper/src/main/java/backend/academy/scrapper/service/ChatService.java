package backend.academy.scrapper.service;

import backend.academy.scrapper.exception.BadRequestException;
import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.record.ChatRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

    public void register(Long id) {
        if (id < 0) {
            throw new BadRequestException(String.format("Невалидный идентификатор чата: %s", id));
        }
        chatRepository.saveUser(id);
    }

    public void unRegister(Long id) {
        if (id < 0) {
            throw new BadRequestException(String.format("Невалидный идентификатор чата: %s", id));
        }
        ChatRecord record = chatRepository.removeUser(id);
        if (record == null) {
            throw new NotFoundException(String.format("Не существует чата с ID: %s", id));
        }
    }
}
