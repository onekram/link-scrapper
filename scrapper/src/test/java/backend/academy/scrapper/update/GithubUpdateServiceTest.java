package backend.academy.scrapper.update;

import static backend.academy.scrapper.test.util.TestUtil.generateLinkRecord;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.client.github.GithubReposClient;
import backend.academy.scrapper.client.model.github.GithubResponse;
import backend.academy.scrapper.client.model.github.GithubUser;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.service.LinksService;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        when(linksService.findAllByType(LinkType.GITHUB))
                .thenReturn(Stream.of(
                        generateLinkRecord("https://github.com/onekram/game", 123L, 453L),
                        generateLinkRecord("https://github.com/onekram/factorization", 453L),
                        generateLinkRecord("https://github.com/onekram/hamarch", 123L)));
        Instant now = Instant.now();
        when(githubReposClient.listIssues(any(), any(), any()))
                .thenReturn(List.of(new GithubResponse("Issue", "url", new GithubUser("login", "url"), now, "body")));

        List<LinkUpdate> actualUpdates = githubUpdateService
                .getLinks()
                .flatMap(githubUpdateService::buildLinkUpdate)
                .toList();

        assertThat(actualUpdates)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(List.of(
                        new LinkUpdate(
                                "https://github.com/onekram/game",
                                "Issue",
                                "url",
                                "login",
                                "url",
                                "body",
                                now,
                                List.of(123L, 453L)),
                        new LinkUpdate(
                                "https://github.com/onekram/factorization",
                                "Issue",
                                "url",
                                "login",
                                "url",
                                "body",
                                now,
                                List.of(453L)),
                        new LinkUpdate(
                                "https://github.com/onekram/hamarch",
                                "Issue",
                                "url",
                                "login",
                                "url",
                                "body",
                                now,
                                List.of(123L))));
    }
}
