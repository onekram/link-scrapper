package backend.academy.scrapper.job;

import backend.academy.scrapper.update.UpdateService;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateCheckerScheduler {
    private final Set<UpdateService> updateServiceSet;

    @Scheduled(fixedRateString = "${app.fixed-rate-scheduling}")
    public void checkRepositoryUpdates() {
        log.info("Start scheduling");
        updateServiceSet.forEach(UpdateService::processUpdate);
    }
}
