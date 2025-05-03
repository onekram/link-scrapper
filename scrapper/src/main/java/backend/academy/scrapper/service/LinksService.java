package backend.academy.scrapper.service;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.EntityByNameFinderAndSaver;
import backend.academy.scrapper.repository.FilterRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.TagRepository;
import backend.academy.scrapper.repository.entity.Chat;
import backend.academy.scrapper.repository.entity.Filter;
import backend.academy.scrapper.repository.entity.Link;
import backend.academy.scrapper.repository.entity.Tag;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LinksService {
    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final TagRepository tagRepository;
    private final FilterRepository filterRepository;

    @Transactional
    public ListLinksResponse listAll(Long tgChatId) {
        return chatRepository
                .findById(tgChatId)
                .map(chat -> new ListLinksResponse(
                        chat.links().stream().map(this::linkToResponse).toList(),
                        chat.links().size()))
                .orElse(new ListLinksResponse(List.of(), 0));
    }

    @Transactional
    public LinkResponse addLink(Long tgChatId, AddLinkRequest request) {
        Chat chat = chatRepository.findById(tgChatId).orElseGet(() -> chatRepository.save(new Chat(tgChatId)));
        Set<Tag> tags = findByNameOrCreate(request.tags(), tagRepository, Tag::new);
        Set<Filter> filters = findByNameOrCreate(request.filters(), filterRepository, Filter::new);

        Link newLink = chat.links().stream()
                .filter(link -> request.link().equals(link.url()))
                .findFirst()
                .orElseGet(() -> linkRepository.save(new Link(request.link(), tags, filters)));

        newLink.tags(tags);
        newLink.filters(filters);

        chat.links().add(newLink);
        newLink.chats().add(chat);
        return linkToResponse(newLink);
    }

    @Transactional
    public LinkResponse removeLink(Long tgChatId, RemoveLinkRequest request) {
        Chat chat = chatRepository
                .findById(tgChatId)
                .orElseThrow(() -> new NotFoundException("Не существует чата: %s".formatted(tgChatId)));

        Link targetLink = chat.links().stream()
                .filter(link -> request.link().equals(link.url()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Не существует ссылки: %s".formatted(request.link())));

        chat.links().remove(targetLink);
        targetLink.chats().remove(chat);

        if (targetLink.chats().isEmpty()) {
            linkRepository.delete(targetLink);
        }
        return linkToResponse(targetLink);
    }

    public List<Link> findAllByType(LinkType linkType) {
        return linkRepository.findAllByType(linkType);
    }

    private LinkResponse linkToResponse(Link link) {
        return new LinkResponse(
                link.id(),
                link.url(),
                link.tags().stream().map(Tag::name).toList(),
                link.filters().stream().map(Filter::name).toList());
    }

    private static <T> Set<T> findByNameOrCreate(
            List<String> names, EntityByNameFinderAndSaver<T> repository, Function<String, T> constructor) {
        return names.stream()
                .map(name -> repository.findByName(name).orElseGet(() -> repository.save(constructor.apply(name))))
                .collect(Collectors.toSet());
    }
}
