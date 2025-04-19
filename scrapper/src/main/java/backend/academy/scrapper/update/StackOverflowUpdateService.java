package backend.academy.scrapper.update;

import backend.academy.scrapper.client.model.StackOverflowQuestionsResponse;
import backend.academy.scrapper.client.stackoverflow.StackOverflowQuestionClient;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.record.LinkRecord;
import backend.academy.scrapper.service.LinksService;
import java.time.Instant;
import java.util.regex.Matcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StackOverflowUpdateService extends AbstractUpdateService {
    private final StackOverflowQuestionClient stackOverflowQuestionClient;

    public StackOverflowUpdateService(
            LinksService linksService, StackOverflowQuestionClient stackOverflowQuestionClient) {
        super(linksService);
        this.stackOverflowQuestionClient = stackOverflowQuestionClient;
    }

    @Override
    protected boolean isUpdated(LinkRecord linkRecord, Instant from) {
        Matcher matcher = LinkType.STACK_OVERFLOW.parseUrl(linkRecord.url().toString());
        if (!matcher.matches()) {
            throw new IllegalStateException("Link of STACK_OVERFLOW type doesn't match pattern");
        }
        String id = matcher.group(1);
        StackOverflowQuestionsResponse response = stackOverflowQuestionClient.getQuestionsByIds(id);
        return response.items().getFirst().lastActivityDate().isAfter(from);
    }

    @Override
    protected LinkType getLinkType() {
        return LinkType.STACK_OVERFLOW;
    }

    @Override
    protected String getMessage() {
        return "StackOverflow updates";
    }
}
