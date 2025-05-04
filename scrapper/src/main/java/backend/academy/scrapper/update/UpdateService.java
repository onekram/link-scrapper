package backend.academy.scrapper.update;

import backend.academy.model.LinkUpdate;
import java.util.List;

public interface UpdateService {
    List<LinkUpdate> getUpdates();
}
