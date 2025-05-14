package backend.academy.scrapper.update;

import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.client.bot.UpdatesClient;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.record.LinkRecord;
import backend.academy.scrapper.service.LinksService;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractUpdateService implements UpdateService {
    protected static final int PREVIEW_LENGTH = 200;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final UpdatesClient updatesClient;
    protected final LinksService linksService;

    public Stream<LinkRecord> getLinks() {
        return linksService.findAllByType(getLinkType());
    }

    @Override
    public void processUpdate() {
        getLinks().forEach(linkRecord -> {
            taskExecutor.execute(() -> {
                try {
                    buildLinkUpdate(linkRecord).forEach(updatesClient::updates);
                } catch (Exception e) {
                    log.error("Error while checking updates for link: {}", linkRecord.url(), e);
                }
            });
        });
    }

    protected abstract LinkType getLinkType();

    protected abstract Stream<LinkUpdate> buildLinkUpdate(LinkRecord linkRecord);
}
