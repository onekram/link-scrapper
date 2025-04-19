package backend.academy.scrapper.repository.record;

import backend.academy.scrapper.parser.LinkType;
import java.net.URI;
import java.util.List;

public record LinkRecord(Long id, URI url, List<String> tags, List<String> filters, LinkType type) {}
