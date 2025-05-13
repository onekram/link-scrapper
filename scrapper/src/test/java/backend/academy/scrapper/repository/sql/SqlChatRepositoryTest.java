package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, SqlChatRepository.class, RepositoryTestHelper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SqlChatRepositoryTest {
    @Autowired
    private RepositoryTestHelper repositoryTestHelper;
    @Autowired
    private SqlChatRepository sqlChatRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void saveIfAbsentByIdAlreadyExist() {
        long chatId = repositoryTestHelper.saveChatById(1L);

        sqlChatRepository.saveIfAbsentById(chatId);

        assertThat(allChats())
            .singleElement()
            .isEqualTo(chatId);
    }

    @Test
    void saveIfAbsentById() {
        sqlChatRepository.saveIfAbsentById(1L);

        assertThat(allChats())
            .singleElement()
            .isEqualTo(1L);
    }

    @Test
    void existsById() {
        assertThat(sqlChatRepository.existsById(1L))
            .isFalse();

        repositoryTestHelper.saveChatById(1L);

        assertThat(sqlChatRepository.existsById(1L))
            .isTrue();
    }

    @Test
    void deleteById() {
        repositoryTestHelper.saveChatById(1L);

        sqlChatRepository.deleteById(1L);
        assertThat(allChats())
            .isEmpty();
    }

    private List<Long> allChats() {
        return jdbcTemplate.queryForList("SELECT id FROM subscription.chat", Long.class);
    }
}
