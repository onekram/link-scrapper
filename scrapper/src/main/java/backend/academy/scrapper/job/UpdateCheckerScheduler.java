package backend.academy.scrapper.job;

import backend.academy.scrapper.client.bot.UpdatesClient;
import backend.academy.scrapper.update.UpdateService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateCheckerScheduler {
    private final Set<UpdateService> updateServiceSet;
    private final UpdatesClient updatesClient;

    @Scheduled(fixedRateString = "${app.fixed-rate-scheduling}")
    public void checkRepositoryUpdates() {
        updateServiceSet.forEach(service -> service.getUpdates().forEach(updatesClient::updates));
    }
}
