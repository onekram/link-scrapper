package backend.academy.scrapper.service;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.scrapper.exception.BadRequestException;
import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinksRepository;
import backend.academy.scrapper.repository.record.LinkRecord;
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
    public static final String INVALID_TG_CHAT_ID_FORMAT_MESSAGE = "Невалидный идентификатор чата: %s";
    private final ChatRepository chatRepository;
    private final LinksRepository linksRepository;

    public ListLinksResponse listAll(Long tgChatId) {
        if (tgChatId < 0) {
            throw new BadRequestException(INVALID_TG_CHAT_ID_FORMAT_MESSAGE.formatted(tgChatId));
        }
        List<LinkResponse> linkResponses = chatRepository.getLinks(tgChatId).stream()
                .map(linksRepository::getLink)
                .filter(Objects::nonNull)
                .map(this::recordToResponse)
                .toList();
        return new ListLinksResponse(linkResponses, linkResponses.size());
    }

    public LinkResponse addLink(Long tgChatId, AddLinkRequest request) {
        if (tgChatId < 0) {
            throw new BadRequestException(INVALID_TG_CHAT_ID_FORMAT_MESSAGE.formatted(tgChatId));
        }
        Long linkRecordId = chatRepository.getLinks(tgChatId).stream()
                .map(linksRepository::getLink)
                .filter(Objects::nonNull)
                .filter(link -> request.link().equals(link.url().toString()))
                .findAny()
                .map(LinkRecord::id)
                .orElseGet(System::currentTimeMillis);
        LinkRecord record = linksRepository.addLink(requestToRecord(linkRecordId, request));
        chatRepository.addLink(tgChatId, linkRecordId);
        return recordToResponse(record);
    }

    public LinkResponse removeLink(Long tgChatId, RemoveLinkRequest request) {
        if (tgChatId < 0) {
            throw new BadRequestException(INVALID_TG_CHAT_ID_FORMAT_MESSAGE.formatted(tgChatId));
        }
        LinkRecord record = chatRepository.getLinks(tgChatId).stream()
                .map(linksRepository::getLink)
                .filter(Objects::nonNull)
                .filter(link -> request.link().equals(link.url().toString()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Не существует ссылки: %s".formatted(request.link())));
        linksRepository.removeLink(record.id());
        return recordToResponse(record);
    }

    public Map<Long, List<LinkRecord>> fetchIdAndLinksByType(LinkType linkType) {
        return chatRepository.fetchAll().stream()
                .collect(Collectors.toMap(Function.identity(), id -> chatRepository.getLinks(id).stream()
                        .map(linksRepository::getLink)
                        .filter(Objects::nonNull)
                        .filter(link -> Objects.equals(link.type(), linkType))
                        .toList()));
    }

    private LinkResponse recordToResponse(LinkRecord record) {
        return new LinkResponse(record.id(), record.url().toString(), record.tags(), record.filters());
    }

    private LinkRecord requestToRecord(Long id, AddLinkRequest request) {
        try {
            LinkType linkType = LinkType.getType(request.link()).orElse(null);
            return new LinkRecord(id, new URI(request.link()), request.tags(), request.filters(), linkType);
        } catch (URISyntaxException | IllegalArgumentException ex) {
            throw new BadRequestException("Невалидная ссылка: %s".formatted(request.link()));
        }
    }
}
