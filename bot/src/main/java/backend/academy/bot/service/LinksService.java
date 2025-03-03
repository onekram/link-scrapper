package backend.academy.bot.service;

import backend.academy.bot.client.LinksClient;
import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinksService {
    private final LinksClient linksClient;

    public ListLinksResponse getTrackedLinks(Long tgChatId) {
        return linksClient.getLinks(tgChatId);
    }

    public LinkResponse trackLink(Long tgChatId, AddLinkRequest request) {
        return linksClient.addLink(tgChatId, request);
    }

    public LinkResponse untrackLink(Long tgChatId, RemoveLinkRequest request) {
        return linksClient.removeLink(tgChatId, request);
    }
}
