package backend.academy.scrapper.update;

import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.repository.record.LinkRecord;
import java.util.stream.Stream;

public interface UpdateService {
    Stream<LinkRecord> getLinks();

    Stream<LinkUpdate> buildLinkUpdate(LinkRecord linkRecord);
}
