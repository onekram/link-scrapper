package backend.academy.model;

import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record LinkUpdate(
        String resourceUrl,
        String title,
        String updateUrl,
        String user,
        String userUrl,
        String description,
        Instant time,
        List<Long> tgChatIds) {}
