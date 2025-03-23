package backend.academy.scrapper.update;

import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.client.github.GithubReposClient;
import backend.academy.scrapper.client.model.RepositoryResponse;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.LinkRecord;
import backend.academy.scrapper.service.LinksService;
import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubUpdateServiceTest {

    @Mock
    private LinksService linksService;

    @Mock
    private GithubReposClient githubReposClient;

    @InjectMocks
    private GithubUpdateService githubUpdateService;

    @Test
    @DisplayName("Correct list of updated links")
    void getListUpdateCorrect() {
        when(linksService.fetchIdAndLinksByType(LinkType.GITHUB)).thenReturn(
            Map.of(
                123L, List.of(generateLinkRecord(1L, "https://github.com/onekram/game"), generateLinkRecord(2L, "https://github.com/onekram/hamarch")),
                453L, List.of(generateLinkRecord(5L, "https://github.com/onekram/factorization"), generateLinkRecord(10L, "https://github.com/onekram/game"))
            )
        );
        Instant now = Instant.now();
        when(githubReposClient.checkForUpdates(any(), any())).thenReturn(new RepositoryResponse(now));

        var actualUpdates = githubUpdateService.getUpdates(now.minusMillis(1000));

        assertThat(actualUpdates)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .ignoringCollectionOrder()
            .isEqualTo(List.of(
                new LinkUpdate(0L, "https://github.com/onekram/game", "Github updates", List.of(123L, 453L)),
                new LinkUpdate(0L, "https://github.com/onekram/factorization", "Github updates", List.of(453L)),
                new LinkUpdate(0L, "https://github.com/onekram/hamarch", "Github updates", List.of(123L))
            ));
    }


    @SneakyThrows
    private LinkRecord generateLinkRecord(Long id, String url) {
        return new LinkRecord(id, new URI(url).toURL(), Collections.emptyList(), Collections.emptyList(), LinkType.GITHUB);
    }
}
