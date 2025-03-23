package backend.academy.scrapper.job;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.client.bot.UpdatesClient;
import backend.academy.scrapper.update.GithubUpdateService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpdateCheckerScheduler {
    private final GithubUpdateService githubUpdateService;
    private final ScrapperConfig scrapperConfig;
    private final UpdatesClient updatesClient;

    @Scheduled(fixedRateString = "${app.fixed-rate-scheduling}")
    public void checkRepositoryUpdates() {
        Instant from = Instant.now().minusMillis(scrapperConfig.fixedRateScheduling());
        githubUpdateService.getUpdates(from).forEach(updatesClient::updates);
    }
}
