package backend.academy.scrapper.service;

import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.model.AddLinkRequest;
import backend.academy.scrapper.repository.LinkRecord;
import backend.academy.scrapper.repository.LinksRepository;
import backend.academy.scrapper.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LinksService {
    private final ChatRepository chatRepository;
    private final LinksRepository linksRepository;

    public ListLinksResponse listAll(Long tgChatId) {
        List<Long> linkIds = chatRepository.getLinks(tgChatId);
        List<LinkResponse> linkResponses = linkIds.stream()
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
                LinkRecord record =  linksRepository.addLink(id, request);
                chatRepository.addLink(tgChatId, record.getId());
                return record;
            });
        linksRepository.addLink(linkRecord.getId(), request);
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

    private LinkResponse recordToResponse(LinkRecord record) {
        return new LinkResponse(record.getId(), record.getUrl().toString(), record.getTags(), record.getFilters());
    }
}
