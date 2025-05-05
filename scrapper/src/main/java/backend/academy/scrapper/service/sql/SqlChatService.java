package backend.academy.scrapper.service.sql;

import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.service.ChatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "features.orm.enabled", havingValue = "false")
public class SqlChatService implements ChatService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void register(Long id) {
        jdbcTemplate.update("INSERT INTO subscription.chat (id) VALUES (?)", id);
    }

    @Override
    @Transactional
    public void unRegister(Long id) {
        try {
            jdbcTemplate.queryForObject("SELECT id FROM subscription.chat WHERE id =?", Long.class, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Не существует чата с ID: %s", id));
        }
        List<Long> linkIds = jdbcTemplate.queryForList(
                """
                SELECT l.id FROM subscription.subscription s
                JOIN subscription.link l ON s.link_id = l.id
                WHERE s.chat_id =?
               """,
                Long.class,
                id);
        jdbcTemplate.update("DELETE FROM subscription.chat WHERE id =?", id);
        linkIds.forEach(linkId -> {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM subscription.subscription WHERE link_id = ?", Integer.class, linkId);
            if (count != null && count == 0) {
                jdbcTemplate.update("DELETE FROM subscription.link WHERE id = ?", linkId);
            }
        });
    }
}
