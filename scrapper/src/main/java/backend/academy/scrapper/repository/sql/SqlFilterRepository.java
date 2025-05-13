package backend.academy.scrapper.repository.sql;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SqlFilterRepository {
    private final JdbcTemplate jdbcTemplate;

    public long saveIfAbsentByName(String name) {
        return jdbcTemplate.queryForObject(
            "INSERT INTO subscription.filter (name) VALUES (?) ON CONFLICT (name) DO UPDATE SET name = excluded.name RETURNING id", Long.class, name);
    }

    public List<String> findAllBySubscriptionId(long subscriptionId) {
        return jdbcTemplate.queryForList(
            "SELECT f.name FROM subscription.filter f "
                + "JOIN subscription.subscription_filter sf ON f.id = sf.filter_id WHERE sf.subscription_id = ?",
            String.class,
            subscriptionId);
    }
}
