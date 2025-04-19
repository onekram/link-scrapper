package backend.academy.scrapper.update;

import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.record.LinkRecord;
import backend.academy.scrapper.service.LinksService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractUpdateService implements UpdateService {
    private final LinksService linksService;

    @Override
    public List<LinkUpdate> getUpdates(Instant from) {
        return linksService.fetchIdAndLinksByType(getLinkType()).entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(link -> Map.entry(entry.getKey(), link)))
                .filter(entry -> isUpdated(entry.getValue(), from))
                .collect(Collectors.groupingBy(
                        entry -> entry.getValue().url().toString(),
                        Collectors.mapping(Map.Entry::getKey, Collectors.toList())))
                .entrySet()
                .stream()
                .map(this::buildLinkUpdate)
                .toList();
    }

    private LinkUpdate buildLinkUpdate(Map.Entry<String, List<Long>> entry) {
        return new LinkUpdate(System.currentTimeMillis(), entry.getKey(), getMessage(), entry.getValue());
    }

    protected abstract boolean isUpdated(LinkRecord linkRecord, Instant from);

    protected abstract LinkType getLinkType();

    protected abstract String getMessage();
}
