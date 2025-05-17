package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.record.LinkRecord;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SqlLinkRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public Long saveIfAbsentByUrl(String url) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO subscription.link (url, type) VALUES (?, ?::subscription.link_type) ON CONFLICT (url) DO UPDATE SET type = excluded.type RETURNING id",
                Long.class,
                url,
                LinkType.getType(url).map(LinkType::name).orElse(null));
    }

    public void updateUpdatedAtByUrl(String url, Instant updatedAt) {
        jdbcTemplate.update("UPDATE subscription.link SET updated_at =? WHERE url =?", Timestamp.from(updatedAt), url);
    }

    public List<LinkRecord> findAllByType(LinkType linkType, int offset, int pageSize) {
        String sql =
                """
                    SELECT l.id AS id,
                           l.url AS url,
                           l.updated_at AS updated_at,
                           array_agg(c.id) AS tg_chat_ids
                    FROM subscription.link l
                    JOIN subscription.subscription s ON l.id = s.link_id
                    JOIN subscription.chat c ON s.chat_id = c.id
                    WHERE l.type = :linkType::subscription.link_type
                    GROUP BY l.id, l.url, l.updated_at
                    ORDER BY l.id
                    LIMIT :limit OFFSET :offset
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("linkType", linkType.name())
                .addValue("limit", pageSize)
                .addValue("offset", offset);
        return namedParameterJdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> new LinkRecord(
                        rs.getString("url"),
                        List.of((Long[]) rs.getArray("tg_chat_ids").getArray()),
                        rs.getTimestamp("updated_at").toInstant()));
    }

    public Long findByUrl(String url) {
        return jdbcTemplate.queryForObject("SELECT id FROM subscription.link WHERE url = ?", Long.class, url);
    }

    public void deleteByIdIfNoAssociatedSubscriptions(long linkId) {
        Integer count = Objects.requireNonNull(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM subscription.subscription WHERE link_id = ?", Integer.class, linkId));
        if (count == 0) {
            jdbcTemplate.update("DELETE FROM subscription.link WHERE id = ?", linkId);
        }
    }

    public List<Long> findAllIdsByChatId(long tgChatId) {
        return jdbcTemplate.queryForList(
                """
                 SELECT l.id FROM subscription.subscription s
                 JOIN subscription.link l ON s.link_id = l.id
                 WHERE s.chat_id =?
                """,
                Long.class,
                tgChatId);
    }

    public List<Result> findAllByChatId(long tgChatId) {
        String sql = "SELECT s.id AS sub_id, l.id AS link_id, l.url AS url "
                + "FROM subscription.subscription s JOIN subscription.link l ON s.link_id = l.id "
                + "JOIN subscription.chat c ON s.chat_id = c.id WHERE c.id = ?";
        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    long subId = rs.getLong("sub_id");
                    long linkId = rs.getLong("link_id");
                    String url = rs.getString("url");
                    return new Result(linkId, url, subId);
                },
                tgChatId);
    }

    public record Result(long linkId, String url, long subscriptionId) {}
}
