package backend.academy.scrapper.repository.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import backend.academy.scrapper.TestcontainersConfiguration;
import backend.academy.scrapper.parser.LinkType;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@JdbcTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, SqlLinkRepository.class, RepositoryTestHelper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SqlLinkRepositoryTest {

    @Autowired
    private SqlLinkRepository sqlLinkRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RepositoryTestHelper repositoryTestHelper;

    @Test
    void saveIfAbsentByUrlAlreadyExist() {
        long linkId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");

        assertThat(sqlLinkRepository.saveIfAbsentByUrl("https://github.com/onekram/game"))
                .isEqualTo(linkId);

        assertThat(repositoryTestHelper.linkCount()).isEqualTo(1);
    }

    @Test
    void saveIfAbsentByUrl() {
        long linkId = sqlLinkRepository.saveIfAbsentByUrl("https://github.com/onekram/game");

        assertThat(repositoryTestHelper.linkCount()).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT type FROM subscription.link WHERE id =?", String.class, linkId))
                .isEqualTo("GITHUB");
    }

    @Test
    void updateUpdatedAtByUrl() {
        long linkId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");
        sqlLinkRepository.updateUpdatedAtByUrl("https://github.com/onekram/game", Instant.now());

        assertThat(jdbcTemplate.queryForObject(
                        "SELECT updated_at FROM subscription.link WHERE id =?", Instant.class, linkId))
                .isCloseTo(Instant.now(), within(4, ChronoUnit.SECONDS));
    }

    @Test
    void findByUrl() {
        long linkId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");
        repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/fractal-flame");

        assertThat(sqlLinkRepository.findByUrl("https://github.com/onekram/game"))
                .isEqualTo(linkId);
    }

    @Test
    void findAllByType() {
        repositoryTestHelper.saveChatById(1L);
        repositoryTestHelper.saveChatById(2L);

        long firstId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");
        long secondId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/fractal-flame");
        repositoryTestHelper.saveLinkByUrl(
                "https://stackoverflow.com/questions/79619962/google-play-console-id-verification-requires-front-and-back-pages-of-my-id-but-i");

        repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(1L, firstId);
        repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(2L, firstId);
        repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(1L, secondId);

        assertThat(sqlLinkRepository.findByUrl("https://github.com/onekram/game"))
                .isEqualTo(firstId);
        assertThat(sqlLinkRepository.findByUrl("https://github.com/onekram/fractal-flame"))
                .isEqualTo(secondId);

        assertThat(sqlLinkRepository.findAllByType(LinkType.GITHUB, 0, 10))
                .hasSize(2)
                .satisfiesExactlyInAnyOrder(
                        link -> {
                            assertThat(link.url()).isEqualTo("https://github.com/onekram/game");
                            assertThat(link.tgChatIds()).containsExactlyInAnyOrder(1L, 2L);
                        },
                        link -> {
                            assertThat(link.url()).isEqualTo("https://github.com/onekram/fractal-flame");
                            assertThat(link.tgChatIds()).containsExactlyInAnyOrder(1L);
                        });
    }

    @Test
    void findAllByTypeSmallPage() {
        repositoryTestHelper.saveChatById(1L);
        repositoryTestHelper.saveChatById(2L);

        long firstId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");
        long secondId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/fractal-flame");
        repositoryTestHelper.saveLinkByUrl(
                "https://stackoverflow.com/questions/79619962/google-play-console-id-verification-requires-front-and-back-pages-of-my-id-but-i");

        repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(1L, firstId);
        repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(2L, firstId);
        repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(1L, secondId);

        assertThat(sqlLinkRepository.findAllByType(LinkType.GITHUB, 0, 1))
                .singleElement()
                .satisfiesAnyOf(
                        link -> {
                            assertThat(link.url()).isEqualTo("https://github.com/onekram/game");
                            assertThat(link.tgChatIds()).containsExactlyInAnyOrder(1L, 2L);
                        },
                        link -> {
                            assertThat(link.url()).isEqualTo("https://github.com/onekram/fractal-flame");
                            assertThat(link.tgChatIds()).containsExactlyInAnyOrder(1L);
                        });
    }

    @Test
    void findAllIdsByChatId() {
        repositoryTestHelper.saveChatById(1L);
        repositoryTestHelper.saveChatById(2L);

        long firstId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");
        long secondId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/fractal-flame");
        repositoryTestHelper.saveLinkByUrl(
                "https://stackoverflow.com/questions/79619962/google-play-console-id-verification-requires-front-and-back-pages-of-my-id-but-i");

        repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(1L, firstId);
        repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(2L, firstId);
        repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(1L, secondId);

        assertThat(sqlLinkRepository.findAllIdsByChatId(1)).hasSize(2).containsExactlyInAnyOrder(firstId, secondId);
    }

    @Test
    void findAllByChatId() {
        repositoryTestHelper.saveChatById(1L);
        repositoryTestHelper.saveChatById(2L);

        long firstId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");
        long secondId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/fractal-flame");
        repositoryTestHelper.saveLinkByUrl(
                "https://stackoverflow.com/questions/79619962/google-play-console-id-verification-requires-front-and-back-pages-of-my-id-but-i");

        long firstSubscription = repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(1L, firstId);
        long secondSubscription = repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(1L, secondId);
        repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(2L, firstId);

        assertThat(sqlLinkRepository.findAllByChatId(1))
                .hasSize(2)
                .satisfiesExactlyInAnyOrder(
                        result -> {
                            assertThat(result.linkId()).isEqualTo(firstId);
                            assertThat(result.url()).isEqualTo("https://github.com/onekram/game");
                            assertThat(result.subscriptionId()).isEqualTo(firstSubscription);
                        },
                        result -> {
                            assertThat(result.linkId()).isEqualTo(secondId);
                            assertThat(result.url()).isEqualTo("https://github.com/onekram/fractal-flame");
                            assertThat(result.subscriptionId()).isEqualTo(secondSubscription);
                        });
    }

    @Test
    void deleteByIdIfNoAssociatedSubscriptions() {
        repositoryTestHelper.saveChatById(1L);
        repositoryTestHelper.saveChatById(2L);

        long firstId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");

        long firstSubscription = repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(1L, firstId);
        long secondSubscription = repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(2L, firstId);

        sqlLinkRepository.deleteByIdIfNoAssociatedSubscriptions(firstId);

        assertThat(repositoryTestHelper.linkCount()).isEqualTo(1);

        repositoryTestHelper.deleteSubscriptionById(firstSubscription);
        repositoryTestHelper.deleteSubscriptionById(secondSubscription);

        sqlLinkRepository.deleteByIdIfNoAssociatedSubscriptions(firstId);
        assertThat(repositoryTestHelper.linkCount()).isEqualTo(0);
    }
}
