package backend.academy.scrapper.service;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.scrapper.client.model.Created;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.record.LinkRecord;
import java.util.stream.Stream;

public interface LinksService {
    ListLinksResponse listAll(Long tgChatId);

    LinkResponse addLink(Long tgChatId, AddLinkRequest request);

    LinkResponse removeLink(Long tgChatId, RemoveLinkRequest request);

    Stream<LinkRecord> findAllByType(LinkType linkType);

    void update(LinkRecord linkRecord, Stream<? extends Created> createdStream);
}
