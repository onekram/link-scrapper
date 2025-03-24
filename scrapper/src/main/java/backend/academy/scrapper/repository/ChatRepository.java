package backend.academy.scrapper.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ChatRepository {
    private final Map<Long, ChatRecord> db = new ConcurrentHashMap<>();

    public List<Long> getLinks(Long id) {
        ChatRecord record = db.get(id);
        if (record == null) {
            return Collections.emptyList();
        }
        return record.getLinks();
    }

    public void saveUser(Long id) {
        db.putIfAbsent(id, new ChatRecord(id, new ArrayList<>()));
    }

    public ChatRecord removeUser(Long id) {
        return db.remove(id);
    }

    public void addLink(Long id, Long linkId) {
        db.computeIfAbsent(id, ignored -> new ChatRecord(id, new ArrayList<>()))
                .getLinks()
                .add(linkId);
    }

    public List<Long> fetchAll() {
        return db.keySet().stream().toList();
    }
}
