package backend.academy.scrapper.repository.record;

import java.util.List;

public record ChatRecord(Long id, List<Long> links) {}
