package backend.academy.scrapper.repository.record;

import java.time.Instant;
import java.util.List;

public record LinkRecord(String url, List<Long> tgChatIds, Instant updatedAt) {}
