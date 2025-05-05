package backend.academy.scrapper.service.sql;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.scrapper.client.model.Created;
import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.record.LinkRecord;
import backend.academy.scrapper.service.LinksService;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "features.orm.enabled", havingValue = "false")
public class SqlLinkService implements LinksService {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Value("${pagination.page-size}")
    private int PAGE_SIZE;

    @Transactional
    @Override
    public ListLinksResponse listAll(Long tgChatId) {
        String sql = "SELECT s.id AS sub_id, l.id AS link_id, l.url AS url "
                + "FROM subscription.subscription s JOIN subscription.link l ON s.link_id = l.id "
                + "JOIN subscription.chat c ON s.chat_id = c.id WHERE c.id = ?";
        List<LinkResponse> links = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> {
                    long subId = rs.getLong("sub_id");
                    long linkId = rs.getLong("link_id");
                    String url = rs.getString("url");
                    List<String> tags = jdbcTemplate.queryForList(
                            "SELECT t.name FROM subscription.tag t "
                                    + "JOIN subscription.subscription_tag st ON t.id = st.tag_id WHERE st.subscription_id = ?",
                            String.class,
                            subId);
                    List<String> filters = jdbcTemplate.queryForList(
                            "SELECT f.name FROM subscription.filter f "
                                    + "JOIN subscription.subscription_filter sf ON f.id = sf.filter_id WHERE sf.subscription_id = ?",
                            String.class,
                            subId);
                    return new LinkResponse(linkId, url, tags, filters);
                },
                tgChatId);
        return new ListLinksResponse(links, links.size());
    }

    @Transactional
    @Override
    public LinkResponse addLink(Long tgChatId, AddLinkRequest request) {
        jdbcTemplate.update("INSERT INTO subscription.chat (id) VALUES (?) ON CONFLICT (id) DO NOTHING", tgChatId);

        jdbcTemplate.update(
                "INSERT INTO subscription.link (url, type) VALUES (?, ?::subscription.link_type) ON CONFLICT (url) DO NOTHING",
                request.link(),
                LinkType.getType(request.link()).map(LinkType::name).orElse(null));

        Long linkId = jdbcTemplate.queryForObject(
                "SELECT id FROM subscription.link WHERE url = ?", Long.class, request.link());

        Set<Long> tagIds = request.tags().stream()
                .map(name -> {
                    jdbcTemplate.update(
                            "INSERT INTO subscription.tag (name) VALUES (?) ON CONFLICT (name) DO NOTHING", name);
                    return jdbcTemplate.queryForObject(
                            "SELECT id FROM subscription.tag WHERE name = ?", Long.class, name);
                })
                .collect(Collectors.toSet());

        Set<Long> filterIds = request.filters().stream()
                .map(name -> {
                    jdbcTemplate.update(
                            "INSERT INTO subscription.filter (name) VALUES (?) ON CONFLICT (name) DO NOTHING", name);
                    return jdbcTemplate.queryForObject(
                            "SELECT id FROM subscription.filter WHERE name = ?", Long.class, name);
                })
                .collect(Collectors.toSet());

        jdbcTemplate.update(
                "INSERT INTO subscription.subscription (chat_id, link_id) VALUES (?, ?) ON CONFLICT (chat_id, link_id) DO NOTHING",
                tgChatId,
                linkId);

        Long subId = jdbcTemplate.queryForObject(
                "SELECT id FROM subscription.subscription WHERE chat_id = ? AND link_id = ?",
                Long.class,
                tgChatId,
                linkId);

        jdbcTemplate.update("DELETE FROM subscription.subscription_tag WHERE subscription_id = ?", subId);
        jdbcTemplate.update("DELETE FROM subscription.subscription_filter WHERE subscription_id = ?", subId);

        tagIds.forEach(tagId -> jdbcTemplate.update(
                "INSERT INTO subscription.subscription_tag (subscription_id, tag_id) VALUES (?, ?)", subId, tagId));
        filterIds.forEach(filterId -> jdbcTemplate.update(
                "INSERT INTO subscription.subscription_filter (subscription_id, filter_id) VALUES (?, ?)",
                subId,
                filterId));

        return new LinkResponse(linkId, request.link(), List.copyOf(request.tags()), List.copyOf(request.filters()));
    }

    @Transactional
    @Override
    public LinkResponse removeLink(Long tgChatId, RemoveLinkRequest request) {
        Long linkId;
        try {
            linkId = jdbcTemplate.queryForObject(
                    "SELECT id FROM subscription.link WHERE url = ?", Long.class, request.link());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Не существует чата: %s".formatted(tgChatId));
        }

        Long subId;
        try {
            subId = jdbcTemplate.queryForObject(
                    "SELECT s.id FROM subscription.subscription s WHERE s.chat_id = ? AND s.link_id = ?",
                    Long.class,
                    tgChatId,
                    linkId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Не существует ссылки: %s".formatted(tgChatId));
        }

        List<String> tags = jdbcTemplate.queryForList(
                "SELECT t.name FROM subscription.tag t " + "JOIN subscription.subscription_tag st ON t.id = st.tag_id "
                        + "WHERE st.subscription_id = ?",
                String.class,
                subId);
        List<String> filters = jdbcTemplate.queryForList(
                "SELECT f.name FROM subscription.filter f "
                        + "JOIN subscription.subscription_filter sf ON f.id = sf.filter_id "
                        + "WHERE sf.subscription_id = ?",
                String.class,
                subId);

        jdbcTemplate.update("DELETE FROM subscription.subscription WHERE id = ?", subId);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM subscription.subscription WHERE link_id = ?", Integer.class, linkId);
        if (count != null && count == 0) {
            jdbcTemplate.update("DELETE FROM subscription.link WHERE id = ?", linkId);
        }

        return new LinkResponse(linkId, request.link(), tags, filters);
    }

    @Override
    public Stream<LinkRecord> findAllByType(LinkType linkType) {
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
        return Stream.iterate(0, n -> n + 1)
                .map(n -> {
                    int offset = n * PAGE_SIZE;
                    MapSqlParameterSource params = new MapSqlParameterSource()
                            .addValue("linkType", linkType.name())
                            .addValue("limit", PAGE_SIZE)
                            .addValue("offset", offset);
                    return namedParameterJdbcTemplate.query(
                            sql,
                            params,
                            (rs, rowNum) -> new LinkRecord(
                                    rs.getString("url"),
                                    List.of((Long[]) rs.getArray("tg_chat_ids").getArray()),
                                    rs.getTimestamp("updated_at").toInstant()));
                })
                .takeWhile(pageList -> !pageList.isEmpty())
                .flatMap(List::stream);
    }

    @Transactional
    @Override
    public void update(LinkRecord linkRecord, Stream<? extends Created> createdStream) {
        Instant max = createdStream
                .map(Created::createdAt)
                .max(Comparator.naturalOrder())
                .orElse(linkRecord.updatedAt());
        jdbcTemplate.update(
                "UPDATE subscription.link SET updated_at =? WHERE url =?", Timestamp.from(max), linkRecord.url());
    }
}
