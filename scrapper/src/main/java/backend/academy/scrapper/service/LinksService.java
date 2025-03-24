package backend.academy.scrapper.service;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.scrapper.exception.BadRequestException;
import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRecord;
import backend.academy.scrapper.repository.LinksRepository;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinksService {
    private final ChatRepository chatRepository;
    private final LinksRepository linksRepository;

    public ListLinksResponse listAll(Long tgChatId) {
        List<LinkResponse> linkResponses = chatRepository.getLinks(tgChatId).stream()
                .map(linksRepository::getLink)
                .filter(Objects::nonNull)
                .map(this::recordToResponse)
                .toList();
        return new ListLinksResponse(linkResponses, linkResponses.size());
    }

    public LinkResponse addLink(Long tgChatId, AddLinkRequest request) {
        LinkRecord linkRecord = chatRepository.getLinks(tgChatId).stream()
                .map(linksRepository::getLink)
                .filter(Objects::nonNull)
                .filter(link -> request.getLink().equals(link.getUrl().toString()))
                .findAny()
                .orElseGet(() -> {
                    long id = System.currentTimeMillis();
                    LinkRecord record = linksRepository.addLink(requestToRecord(id, request));
                    chatRepository.addLink(tgChatId, record.getId());
                    return record;
                });
        linksRepository.addLink(requestToRecord(linkRecord.getId(), request));
        return recordToResponse(linkRecord);
    }

    public LinkResponse removeLink(Long tgChatId, RemoveLinkRequest request) {
        LinkRecord record = chatRepository.getLinks(tgChatId).stream()
                .map(linksRepository::getLink)
                .filter(Objects::nonNull)
                .filter(link -> request.getLink().equals(link.getUrl().toString()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Не существует ссылки: %s", request.getLink())));
        linksRepository.removeLink(record.getId());
        return recordToResponse(record);
    }

    public Map<Long, List<LinkRecord>> fetchIdAndLinksByType(LinkType linkType) {
        return chatRepository.fetchAll().stream()
                .collect(Collectors.toMap(Function.identity(), id -> chatRepository.getLinks(id).stream()
                        .map(linksRepository::getLink)
                        .filter(Objects::nonNull)
                        .filter(link -> Objects.equals(link.getType(), linkType))
                        .toList()));
    }

    private LinkResponse recordToResponse(LinkRecord record) {
        return new LinkResponse(record.getId(), record.getUrl().toString(), record.getTags(), record.getFilters());
    }

    private LinkRecord requestToRecord(Long id, AddLinkRequest request) {
        try {
            LinkType linkType = LinkType.getType(request.getLink()).orElse(null);
            return new LinkRecord(id, new URI(request.getLink()), request.getTags(), request.getFilters(), linkType);
        } catch (URISyntaxException | IllegalArgumentException ex) {
            throw new BadRequestException(String.format("Невалидная ссылка: %s", request.getLink()));
        }
    }
}
