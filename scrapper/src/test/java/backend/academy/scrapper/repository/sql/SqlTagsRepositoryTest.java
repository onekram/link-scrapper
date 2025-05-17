package backend.academy.scrapper.repository.sql;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@JdbcTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, SqlTagRepository.class, RepositoryTestHelper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SqlTagsRepositoryTest {

    @Autowired
    private RepositoryTestHelper repositoryTestHelper;

    @Autowired
    private SqlTagRepository sqlTagsRepositoryTest;

    @Test
    void saveIfAbsentByName() {
        long oldT1 = repositoryTestHelper.saveTagByName("t1");

        assertThat(sqlTagsRepositoryTest.saveIfAbsentByName("t1")).isEqualTo(oldT1);
    }

    @Test
    void findAllBySubscriptionId() {
        long t1 = repositoryTestHelper.saveTagByName("f1");
        long t2 = repositoryTestHelper.saveTagByName("f2");
        long t3 = repositoryTestHelper.saveTagByName("f3");
        long chatId = repositoryTestHelper.saveChatById(1L);
        long linkId = repositoryTestHelper.saveLinkByUrl("https://github.com/onekram/game");

        long s1 = repositoryTestHelper.saveSubscriptionByChatIdAndLinkId(chatId, linkId);
        repositoryTestHelper.associateTagsWithSubscription(s1, t1, t2, t3);

        assertThat(sqlTagsRepositoryTest.findAllBySubscriptionId(s1))
                .hasSize(3)
                .containsExactlyInAnyOrder("f1", "f2", "f3");
    }
}
