package backend.academy.scrapper.update;

import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.client.model.StackOverflowQuestionsResponse;
import backend.academy.scrapper.client.stackoverflow.StackOverflowQuestionClient;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.entity.Chat;
import backend.academy.scrapper.repository.entity.Link;
import backend.academy.scrapper.repository.entity.Subscription;
import backend.academy.scrapper.service.LinksService;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.stream.Stream;
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

    private boolean isUpdated(Link link, Instant from) {
        Matcher matcher = LinkType.STACK_OVERFLOW.parseUrl(link.url());
        if (!matcher.matches()) {
            throw new IllegalStateException("Link of STACK_OVERFLOW type doesn't match pattern");
        }
        String id = matcher.group(1);
        StackOverflowQuestionsResponse response = stackOverflowQuestionClient.getQuestionsByIds(id);
        return response.items().getFirst().lastActivityDate().isAfter(from);
    }

    @Override
    protected Stream<LinkUpdate> buildLinkUpdate(Link link) {
        Instant from = link.updatedAt() == null ? Instant.now() : link.updatedAt();

        if (!isUpdated(link, from)) {
            return null;
        }
        return Stream.of(new LinkUpdate(
            link.url(),
            getMessage(),
            "url",
            "user",
            "userUrl",
            getMessage(),
            Instant.now(),
            link.subscriptions().stream()
                .map(Subscription::chat)
                .map(Chat::id)
                .toList()));
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
