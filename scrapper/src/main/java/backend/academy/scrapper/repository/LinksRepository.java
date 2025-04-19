package backend.academy.scrapper.repository;

import backend.academy.scrapper.repository.record.LinkRecord;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class LinksRepository {
    private final Map<Long, LinkRecord> db = new ConcurrentHashMap<>();

    public LinkRecord addLink(LinkRecord linkRecord) {
        db.put(linkRecord.id(), linkRecord);
        return linkRecord;
    }

    public void removeLink(Long id) {
        db.remove(id);
    }

    public LinkRecord getLink(Long id) {
        return db.get(id);
    }
}
