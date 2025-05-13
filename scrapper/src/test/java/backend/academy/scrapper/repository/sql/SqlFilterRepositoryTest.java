package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, SqlFilterRepository.class, RepositoryTestHelper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SqlFilterRepositoryTest {

    @Autowired
    private RepositoryTestHelper repositoryTestHelper;
    @Autowired
    private SqlFilterRepository sqlFilterRepository;

    @Test
    void saveIfAbsentByName() {
        long oldF1 = repositoryTestHelper.saveFilterByName("f1");

        assertThat(sqlFilterRepository.saveIfAbsentByName("f1"))
            .isEqualTo(oldF1);
    }

    @Test
    void findAllBySubscriptionId() {
        long f1 = repositoryTestHelper.saveFilterByName("f1");
        long f2 = repositoryTestHelper.saveFilterByName("f2");
        long f3 = repositoryTestHelper.saveFilterByName("f3");
        long chatId = repositoryTestHelper.saveChatById(1L);
        long linkId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");

        long s1 = repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(chatId, linkId);
        repositoryTestHelper.associateFiltersWithSubscription(s1, f1, f2, f3);

        assertThat(sqlFilterRepository.findAllBySubscriptionId(s1))
            .hasSize(3)
            .containsExactlyInAnyOrder("f1", "f2", "f3");
    }

}
