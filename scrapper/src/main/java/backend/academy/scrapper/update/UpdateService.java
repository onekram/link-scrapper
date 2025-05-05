package backend.academy.scrapper.update;

import backend.academy.model.LinkUpdate;
import java.util.stream.Stream;

public interface UpdateService {
    Stream<LinkUpdate> getUpdates();
}
