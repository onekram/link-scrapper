package backend.academy.scrapper.update;

import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.client.model.stackoverflow.Answer;
import backend.academy.scrapper.client.model.stackoverflow.Question;
import backend.academy.scrapper.client.model.stackoverflow.Response;
import backend.academy.scrapper.client.stackoverflow.StackOverflowQuestionClient;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.record.LinkRecord;
import backend.academy.scrapper.service.LinksService;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    public Stream<LinkUpdate> buildLinkUpdate(LinkRecord linkRecord) {
        Instant from = linkRecord.updatedAt();

        Matcher matcher = LinkType.STACK_OVERFLOW.parseUrl(linkRecord.url());
        if (!matcher.matches()) {
            throw new IllegalStateException("Link of STACK_OVERFLOW type doesn't match pattern");
        }
        String id = matcher.group(1);
        Question question =
                stackOverflowQuestionClient.getQuestionsByIds(id).items().getFirst();
        String title = question.title();

        linksService.update(linkRecord, generateResponses(question));

        return generateResponses(question)
                .parallel()
                .filter(r -> r.createdAt().isAfter(from))
                .map(r -> LinkUpdate.builder()
                        .resourceUrl(linkRecord.url())
                        .title(title)
                        .updateUrl(r.link())
                        .user(r.owner().displayName())
                        .userUrl(r.owner().link())
                        .description(StringUtils.left(r.bodyMarkdown(), PREVIEW_LENGTH))
                        .time(r.createdAt())
                        .tgChatIds(linkRecord.tgChatIds())
                        .build());
    }

    private Stream<Response> generateResponses(Question question) {
        return Stream.of(
                        question.comments(),
                        question.answers().stream()
                                .map(Answer::comments)
                                .flatMap(List::stream)
                                .toList(),
                        question.answers())
                .flatMap(List::stream);
    }

    @Override
    protected LinkType getLinkType() {
        return LinkType.STACK_OVERFLOW;
    }
}
