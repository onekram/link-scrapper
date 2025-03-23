package backend.academy.scrapper.update;

import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.LinkRecord;
import backend.academy.scrapper.service.LinksService;
import lombok.RequiredArgsConstructor;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class AbstractUpdateService implements UpdateService {
    private final LinksService linksService;

    public List<LinkUpdate> getUpdates(Instant from) {
        return linksService.fetchIdAndLinksByType(getLinkType()).entrySet().stream()
            .flatMap(entry -> entry.getValue().stream()
                .map(link -> Map.entry(entry.getKey(), link)))
            .filter(entry -> isUpdated(entry.getValue(), from))
            .collect(Collectors.groupingBy(entry -> entry.getValue().getUrl().toString(),
                Collectors.mapping(Map.Entry::getKey, Collectors.toList()))).entrySet().stream()
            .map(entry -> new LinkUpdate(System.currentTimeMillis(), entry.getKey(), getMessage(), entry.getValue()))
            .toList();
    }

    protected abstract boolean isUpdated(LinkRecord linkRecord, Instant from);

    protected abstract LinkType getLinkType();

    protected abstract String getMessage();
}
