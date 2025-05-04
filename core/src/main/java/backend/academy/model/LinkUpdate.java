package backend.academy.model;

import lombok.Builder;
import java.time.Instant;
import java.util.List;

@Builder
public record LinkUpdate(
    String resourceUrl,
    String title,
    String updateUrl,
    String user,
    String userUrl,
    String description,
    Instant updatedAt,
    List<Long> tgChatIds
) {

    public LinkUpdate(String resourceUrl, String description, List<Long> tgChatIds) {
        this(resourceUrl, "New update received", "", "Somebody", "", description, Instant.now(), tgChatIds);
    }
}
