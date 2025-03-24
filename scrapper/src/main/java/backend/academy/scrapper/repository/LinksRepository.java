package backend.academy.scrapper.repository;

import backend.academy.model.AddLinkRequest;
import backend.academy.scrapper.exception.BadRequestException;
import backend.academy.scrapper.parser.LinkType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class LinksRepository {
    private final Map<Long, LinkRecord> db = new ConcurrentHashMap<>();

    public LinkRecord addLink(Long id, AddLinkRequest request) {
        try {
            LinkType linkType = LinkType.getType(request.getLink()).orElse(null);
            LinkRecord linkRecord =
                    new LinkRecord(id, new URI(request.getLink()), request.getTags(), request.getFilters(), linkType);
            db.put(id, linkRecord);
            return linkRecord;
        } catch (URISyntaxException | IllegalArgumentException ex) {
            throw new BadRequestException(String.format("Невалидная ссылка: %s", request.getLink()));
        }
    }

    public void removeLink(Long id) {
        db.remove(id);
    }

    public LinkRecord getLink(Long id) {
        return db.get(id);
    }
}
