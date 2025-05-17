package backend.academy.scrapper.repository.sql;

import backend.academy.scrapper.parser.LinkType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RepositoryTestHelper {

    private final JdbcTemplate jdbcTemplate;

    public long saveChatById(Long tgChatId) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO subscription.chat (id) VALUES (?) RETURNING id", Long.class, tgChatId);
    }

    public long saveLinkByUrl(String url) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO subscription.link (url, type) VALUES (?, ?::subscription.link_type) RETURNING id",
                Long.class,
                url,
                LinkType.getType(url).map(LinkType::name).orElse(null));
    }

    public long saveSubscriptionByChatIdAndLinkId(Long tgChatId, Long linkId) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO subscription.subscription (chat_id, link_id) VALUES (?, ?) RETURNING id",
                Long.class,
                tgChatId,
                linkId);
    }

    public void deleteSubscriptionById(long subscriptionId) {
        jdbcTemplate.update("DELETE FROM subscription.subscription WHERE id = ?", subscriptionId);
    }

    public int linkCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM subscription.link", Integer.class);
    }

    public long saveTagByName(String name) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO subscription.tag (name) VALUES (?) RETURNING id", Long.class, name);
    }

    public long saveFilterByName(String name) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO subscription.filter (name) VALUES (?) RETURNING id", Long.class, name);
    }

    public List<Long> allFilters() {
        return jdbcTemplate.queryForList("SELECT ID FROM subscription.filter", Long.class);
    }

    public List<Long> allTags() {
        return jdbcTemplate.queryForList("SELECT ID FROM subscription.tag", Long.class);
    }

    public void associateTagsWithSubscription(Long subscriptionId, long... tagIds) {
        for (long tagId : tagIds) {
            jdbcTemplate.update(
                    "INSERT INTO subscription.subscription_tag (subscription_id, tag_id) VALUES (?,?)",
                    subscriptionId,
                    tagId);
        }
    }

    public void associateFiltersWithSubscription(Long subscriptionId, long... filterIds) {
        for (long filterId : filterIds) {
            jdbcTemplate.update(
                    "INSERT INTO subscription.subscription_filter (subscription_id, filter_id) VALUES (?,?)",
                    subscriptionId,
                    filterId);
        }
    }
}
