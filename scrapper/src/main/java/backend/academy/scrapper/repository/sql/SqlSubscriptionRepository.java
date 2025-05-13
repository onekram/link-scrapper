package backend.academy.scrapper.repository.sql;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SqlSubscriptionRepository {

    private final JdbcTemplate jdbcTemplate;

    public long saveIfAbsentByChatIdAndLinkId(long tgChatId, long linkId) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO subscription.subscription (chat_id, link_id) VALUES (?, ?) ON CONFLICT (chat_id, link_id) DO UPDATE SET chat_id = excluded.chat_id RETURNING id",
                Long.class,
                tgChatId,
                linkId);
    }

    public void deleteAssociationTags(long subscriptionId) {
        jdbcTemplate.update("DELETE FROM subscription.subscription_tag WHERE subscription_id = ?", subscriptionId);
    }

    public void deleteAssociationFilters(long subscriptionId) {
        jdbcTemplate.update("DELETE FROM subscription.subscription_filter WHERE subscription_id = ?", subscriptionId);
    }

    public void associateTags(long subscriptionId, Iterable<Long> tagIds) {
        tagIds.forEach(tagId -> jdbcTemplate.update(
                "INSERT INTO subscription.subscription_tag (subscription_id, tag_id) VALUES (?, ?)",
                subscriptionId,
                tagId));
    }

    public void associateFilters(long subscriptionId, Iterable<Long> filterIds) {
        filterIds.forEach(filterId -> jdbcTemplate.update(
                "INSERT INTO subscription.subscription_filter (subscription_id, filter_id) VALUES (?, ?)",
                subscriptionId,
                filterId));
    }

    public long findByChatIdAndLinkId(long tgChatId, long linkId) {
        return jdbcTemplate.queryForObject(
                "SELECT s.id FROM subscription.subscription s WHERE s.chat_id = ? AND s.link_id = ?",
                Long.class,
                tgChatId,
                linkId);
    }

    public void deleteById(long subscriptionId) {
        jdbcTemplate.update("DELETE FROM subscription.subscription WHERE id = ?", subscriptionId);
    }
}
