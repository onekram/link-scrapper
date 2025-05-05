package backend.academy.scrapper.update;

import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.record.LinkRecord;
import backend.academy.scrapper.service.LinksService;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public abstract class AbstractUpdateService implements UpdateService {
    protected static final int PREVIEW_LENGTH = 200;

    protected final LinksService linksService;

    @Override
    @Transactional
    public Stream<LinkRecord> getLinks() {
        return linksService.findAllByType(getLinkType());
    }

    protected abstract LinkType getLinkType();
}
