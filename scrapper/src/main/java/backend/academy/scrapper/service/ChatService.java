package backend.academy.scrapper.service;

import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.entity.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;

    public void register(Long id) {
        chatRepository.save(new Chat(id));
    }

    @Transactional
    public void unRegister(Long id) {
        if (chatRepository.existsChatById(id)) {
            chatRepository.deleteChatById(id);
        } else {
            throw new NotFoundException(String.format("Не существует чата с ID: %s", id));
        }
    }
}
