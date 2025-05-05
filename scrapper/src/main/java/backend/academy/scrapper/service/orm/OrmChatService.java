package backend.academy.scrapper.service.orm;

import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.entity.Chat;
import backend.academy.scrapper.repository.entity.Link;
import backend.academy.scrapper.repository.entity.Subscription;
import backend.academy.scrapper.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "features.orm.enabled", havingValue = "true", matchIfMissing = false)
public class OrmChatService implements ChatService {
    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;

    @Override
    @Transactional
    public void register(Long id) {
        if (!chatRepository.existsChatById(id)) {
            chatRepository.save(new Chat(id));
        }
    }

    @Transactional
    @Override
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
