package backend.academy.scrapper.service;

import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.entity.Chat;
import backend.academy.scrapper.repository.entity.Link;
import backend.academy.scrapper.repository.entity.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;

    public void register(Long id) {
        chatRepository.save(new Chat(id));
    }

    @Transactional
    public void unRegister(Long id) {
        Chat chat = chatRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Не существует чата с ID: %s", id)));
        chat.subscriptions().forEach(this::deleteSubscription);
        chatRepository.delete(chat);
    }

    private void deleteSubscription(Subscription subscription) {
        Link link = subscription.link();
        link.subscriptions().remove(subscription);
        if (link.subscriptions().isEmpty()) {
            linkRepository.delete(link);
        }
    }
}
