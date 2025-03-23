package backend.academy.scrapper.update;

import backend.academy.model.LinkUpdate;
import java.time.Instant;
import java.util.List;

public interface UpdateService {
    List<LinkUpdate> getUpdates(Instant from);
}
