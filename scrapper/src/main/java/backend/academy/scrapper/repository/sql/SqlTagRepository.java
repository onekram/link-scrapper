package backend.academy.scrapper.repository.sql;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SqlTagRepository {
    private final JdbcTemplate jdbcTemplate;

    public Long saveIfAbsentByName(String name) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO subscription.tag (name) VALUES (?) ON CONFLICT (name) DO UPDATE SET name = excluded.name RETURNING id",
                Long.class,
                name);
    }

    public List<String> findAllBySubscriptionId(long subscriptionId) {
        return jdbcTemplate.queryForList(
                "SELECT t.name FROM subscription.tag t "
                        + "JOIN subscription.subscription_tag st ON t.id = st.tag_id WHERE st.subscription_id = ?",
                String.class,
                subscriptionId);
    }
}
