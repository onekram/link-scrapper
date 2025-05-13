package backend.academy.scrapper.repository.sql;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SqlChatRepository {

    private final JdbcTemplate jdbcTemplate;

    public void saveIfAbsentById(Long tgChatId) {
        jdbcTemplate.update("INSERT INTO subscription.chat (id) VALUES (?) ON CONFLICT (id) DO NOTHING", tgChatId);
    }

    public boolean existsById(Long tgChatId) {
        try {
            jdbcTemplate.queryForObject("SELECT id FROM subscription.chat WHERE id =?", Long.class, tgChatId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    public void deleteById(Long tgChatId) {
        jdbcTemplate.update("DELETE FROM subscription.chat WHERE id =?", tgChatId);
    }
}
