package backend.academy.scrapper.update;

import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.entity.Chat;
import backend.academy.scrapper.repository.entity.Link;
import backend.academy.scrapper.service.LinksService;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractUpdateService implements UpdateService {
    private final LinksService linksService;

    @Override
    public List<LinkUpdate> getUpdates(Instant from) {
        return linksService.findAllByType(getLinkType()).stream()
                .filter(link -> isUpdated(link, from))
                .map(this::buildLinkUpdate)
                .toList();
    }

    private LinkUpdate buildLinkUpdate(Link link) {
        return new LinkUpdate(
                System.currentTimeMillis(),
                link.url(),
                getMessage(),
                link.chats().stream().map(Chat::id).toList());
    }

    protected abstract boolean isUpdated(Link link, Instant from);

    protected abstract LinkType getLinkType();

    protected abstract String getMessage();
}
