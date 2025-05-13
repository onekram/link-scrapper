package backend.academy.scrapper.repository.sql;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.TestcontainersConfiguration;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@JdbcTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, SqlSubscriptionRepository.class, RepositoryTestHelper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SqlSubscriptionRepositoryTest {

    @Autowired
    private RepositoryTestHelper repositoryTestHelper;

    @Autowired
    private SqlSubscriptionRepository sqlSubscriptionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void saveIfAbsentByChatIdAndLinkIdAlreadyExist() {
        long chatId = repositoryTestHelper.saveChatById(1L);
        long linkId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");
        long subscriptionId = repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(chatId, linkId);

        assertThat(sqlSubscriptionRepository.saveIfAbsentByChatIdAndLinkId(chatId, linkId))
                .isEqualTo(subscriptionId);

        assertThat(allSubscriptions()).singleElement().isEqualTo(subscriptionId);
    }

    @Test
    void saveIfAbsentByChatIdAndLinkId() {
        long chatId = repositoryTestHelper.saveChatById(1L);
        long linkId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");
        long subscriptionId = sqlSubscriptionRepository.saveIfAbsentByChatIdAndLinkId(chatId, linkId);

        assertThat(allSubscriptions()).singleElement().isEqualTo(subscriptionId);
    }

    @Test
    void deleteAssociationTags() {
        long chatId = repositoryTestHelper.saveChatById(1L);
        long linkId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");

        long f1 = repositoryTestHelper.saveFilterByName("f1");
        long f2 = repositoryTestHelper.saveFilterByName("f2");

        long t1 = repositoryTestHelper.saveTagByName("t1");
        long t2 = repositoryTestHelper.saveTagByName("t2");

        long s1 = repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(chatId, linkId);

        repositoryTestHelper.associateTagsWithSubscription(s1, t1, t2);
        repositoryTestHelper.associateFiltersWithSubscription(s1, f1, f2);

        sqlSubscriptionRepository.deleteAssociationTags(s1);

        assertThat(repositoryTestHelper.allTags()).hasSize(2);
        assertThat(repositoryTestHelper.allFilters()).hasSize(2);

        assertThat(findTagsBySubscription(s1)).isEmpty();
        assertThat(findFiltersBySubscription(s1)).hasSize(2);
    }

    @Test
    void deleteAssociationFilters() {
        long chatId = repositoryTestHelper.saveChatById(1L);
        long linkId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");

        long f1 = repositoryTestHelper.saveFilterByName("f1");
        long f2 = repositoryTestHelper.saveFilterByName("f2");

        long t1 = repositoryTestHelper.saveTagByName("t1");
        long t2 = repositoryTestHelper.saveTagByName("t2");

        long s1 = repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(chatId, linkId);

        repositoryTestHelper.associateTagsWithSubscription(s1, t1, t2);
        repositoryTestHelper.associateFiltersWithSubscription(s1, f1, f2);

        sqlSubscriptionRepository.deleteAssociationFilters(s1);

        assertThat(repositoryTestHelper.allTags()).hasSize(2);
        assertThat(repositoryTestHelper.allFilters()).hasSize(2);

        assertThat(findTagsBySubscription(s1)).hasSize(2);
        assertThat(findFiltersBySubscription(s1)).isEmpty();
    }

    @Test
    void associateTags() {
        long chatId = repositoryTestHelper.saveChatById(1L);
        long linkId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");
        long t1 = repositoryTestHelper.saveTagByName("t1");
        long t2 = repositoryTestHelper.saveTagByName("t2");
        long s1 = repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(chatId, linkId);

        sqlSubscriptionRepository.associateTags(s1, List.of(t1, t2));

        assertThat(findTagsBySubscription(s1)).hasSize(2).containsExactlyInAnyOrder(t1, t2);
    }

    @Test
    void associateFilters() {
        long chatId = repositoryTestHelper.saveChatById(1L);
        long linkId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");
        long f1 = repositoryTestHelper.saveFilterByName("f1");
        long f2 = repositoryTestHelper.saveFilterByName("f2");
        long s1 = repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(chatId, linkId);

        sqlSubscriptionRepository.associateFilters(s1, List.of(f1, f2));

        assertThat(findFiltersBySubscription(s1)).hasSize(2).containsExactlyInAnyOrder(f1, f2);
    }

    @Test
    void findByChatIdAndLinkId() {
        long chatId = repositoryTestHelper.saveChatById(1L);
        long linkId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");
        long s1 = repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(chatId, linkId);

        assertThat(sqlSubscriptionRepository.findByChatIdAndLinkId(chatId, linkId))
                .isEqualTo(s1);
    }

    @Test
    void deleteById() {
        long chatId = repositoryTestHelper.saveChatById(1L);
        long linkId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");
        long s1 = repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(chatId, linkId);

        sqlSubscriptionRepository.deleteById(s1);

        assertThat(allSubscriptions()).isEmpty();
    }

    private List<Long> allSubscriptions() {
        return jdbcTemplate.queryForList("SELECT ID FROM subscription.subscription", Long.class);
    }

    private @NotNull List<Long> findTagsBySubscription(long s1) {
        return jdbcTemplate.queryForList(
                "SELECT tag_id FROM subscription.subscription_tag WHERE subscription_id =?", Long.class, s1);
    }

    private @NotNull List<Long> findFiltersBySubscription(long s1) {
        return jdbcTemplate.queryForList(
                "SELECT filter_id FROM subscription.subscription_filter WHERE subscription_id =?", Long.class, s1);
    }
}
