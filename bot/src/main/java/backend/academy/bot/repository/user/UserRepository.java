package backend.academy.bot.repository.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final Map<String, UserRecord> db = new ConcurrentHashMap<>();

    public boolean exist(String login) {
        return db.containsKey(login);
    }

    public List<String> getTrackList(String login) {
        return getUserRecord(login).getTrackList();
    }

    public void saveUser(String login, UserRecord userRecord) {
        db.put(login, userRecord);
    }

    private UserRecord getUserRecord(String login) {
        return db.computeIfAbsent(login, key -> new UserRecord(new ArrayList<>()));
    }
}
