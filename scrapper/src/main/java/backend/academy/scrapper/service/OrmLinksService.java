package backend.academy.scrapper.service;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.scrapper.client.model.Created;
import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.EntityByNameFinderAndSaver;
import backend.academy.scrapper.repository.FilterRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.SubscriptionRepository;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.repository.entity.Chat;
import backend.academy.scrapper.repository.entity.Filter;
import backend.academy.scrapper.repository.entity.Link;
import backend.academy.scrapper.repository.entity.Subscription;
import backend.academy.scrapper.repository.entity.Tag;
import backend.academy.scrapper.repository.record.LinkRecord;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "features.orm.enabled", havingValue = "true", matchIfMissing = true)
public class OrmLinksService implements LinksService {
    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final TagRepository tagRepository;
    private final FilterRepository filterRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    @Override
    public ListLinksResponse listAll(Long tgChatId) {
        return chatRepository
                .findById(tgChatId)
                .map(chat -> new ListLinksResponse(
                        chat.subscriptions().stream()
                                .map(this::createLinkResponse)
                                .toList(),
                        chat.subscriptions().size()))
                .orElse(new ListLinksResponse(List.of(), 0));
    }

    @Transactional
    @Override
    public LinkResponse addLink(Long tgChatId, AddLinkRequest request) {
        Chat chat = chatRepository.findById(tgChatId).orElseGet(() -> chatRepository.save(new Chat(tgChatId)));
        Link link =
                linkRepository.findByUrl(request.link()).orElseGet(() -> linkRepository.save(new Link(request.link())));
        Set<Tag> tags = findByNameOrCreate(request.tags(), tagRepository, Tag::new);
        Set<Filter> filters = findByNameOrCreate(request.filters(), filterRepository, Filter::new);

        Subscription subscription = subscriptionRepository
                .findByChatAndLink(chat, link)
                .orElseGet(() -> subscriptionRepository.save(new Subscription(chat, link)));

        subscription.tags(tags);
        subscription.filters(filters);

        return createLinkResponse(subscription);
    }

    @Transactional
    @Override
    public LinkResponse removeLink(Long tgChatId, RemoveLinkRequest request) {
        Chat chat = chatRepository
                .findById(tgChatId)
                .orElseThrow(() -> new NotFoundException("Не существует чата: %s".formatted(tgChatId)));
        Link link = linkRepository
                .findByUrl(request.link())
                .orElseThrow(() -> new NotFoundException("Не существует ссылки: %s".formatted(request.link())));
        Subscription subscription = subscriptionRepository
                .findByChatAndLink(chat, link)
                .orElseThrow(() -> new NotFoundException("Не существует ссылки: %s".formatted(request.link())));

        LinkResponse linkResponse = createLinkResponse(subscription);

        link.subscriptions().remove(subscription);
        if (link.subscriptions().isEmpty()) {
            linkRepository.delete(link);
        }
        return linkResponse;
    }

    @Override
    public List<LinkRecord> findAllByType(LinkType linkType) {
        return linkRepository.findAllByType(linkType).stream()
                .map(link -> new LinkRecord(link.url(), link.getTgChatIds(), link.updatedAt()))
                .toList();
    }

    @Transactional
    @Override
    public void update(LinkRecord linkRecord, Stream<? extends Created> createdStream) {
        Link link = linkRepository.findByUrl(linkRecord.url()).orElseThrow();
        link.setUpdatedAt(createdStream);
    }

    private LinkResponse createLinkResponse(Subscription subscription) {
        return new LinkResponse(
                subscription.link().id(),
                subscription.link().url(),
                subscription.tags().stream().map(Tag::name).toList(),
                subscription.filters().stream().map(Filter::name).toList());
    }

    private static <T> Set<T> findByNameOrCreate(
            List<String> names, EntityByNameFinderAndSaver<T> repository, Function<String, T> constructor) {
        return names.stream()
                .map(name -> repository.findByName(name).orElseGet(() -> repository.save(constructor.apply(name))))
                .collect(Collectors.toSet());
    }
}
