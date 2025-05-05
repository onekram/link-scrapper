package backend.academy.scrapper.job;

import backend.academy.scrapper.client.bot.UpdatesClient;
import backend.academy.scrapper.update.UpdateService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateCheckerScheduler {
    private final Set<UpdateService> updateServiceSet;
    private final UpdatesClient updatesClient;
    private final ThreadPoolTaskExecutor taskExecutor;

    @Scheduled(fixedRateString = "${app.fixed-rate-scheduling}")
    public void checkRepositoryUpdates() {
        log.info("Start scheduling");
        updateServiceSet.forEach(service -> service.getLinks().forEach(linkRecord -> {
            taskExecutor.execute(() -> {
                try {
                    service.buildLinkUpdate(linkRecord).forEach(updatesClient::updates);
                } catch (Exception e) {
                    log.error("Error while checking updates for link: {}", linkRecord.url(), e);
                }
            });
        }));
    }
}
