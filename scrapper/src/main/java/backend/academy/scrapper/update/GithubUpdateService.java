package backend.academy.scrapper.update;

import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.client.github.GithubReposClient;
import backend.academy.scrapper.client.model.github.GithubResponse;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.entity.Chat;
import backend.academy.scrapper.repository.entity.Link;
import backend.academy.scrapper.repository.entity.Subscription;
import backend.academy.scrapper.service.LinksService;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Service;

@Service
public class GithubUpdateService extends AbstractUpdateService {
    public static final int PREVIEW_LENGTH = 200;
    private final GithubReposClient githubReposClient;

    public GithubUpdateService(LinksService linksService, GithubReposClient githubReposClient) {
        super(linksService);
        this.githubReposClient = githubReposClient;
    }

    @Override
    protected Stream<LinkUpdate> buildLinkUpdate(Link link) {
        Matcher matcher = LinkType.GITHUB.parseUrl(link.url());
        if (!matcher.matches()) {
            throw new IllegalStateException("Link of GITHUB type doesn't match pattern");
        }
        String owner = matcher.group(1);
        String repo = matcher.group(2);

        Instant from = link.updatedAt();
        link.updatedAt(Instant.now());

        List<Long> chatIds = link.subscriptions().stream()
            .map(Subscription::chat)
            .map(Chat::id)
            .toList();

        List<GithubResponse> githubPrResponses = githubReposClient.listPulls(owner, repo);
        List<GithubResponse> githubIssueResponses = githubReposClient.listIssues(owner, repo);

        link.updatedAt(Stream.of(githubPrResponses, githubIssueResponses)
            .flatMap(List::stream)
            .map(GithubResponse::updatedAt)
            .max(Comparator.naturalOrder()).orElse(link.updatedAt()));

        return Stream.of(githubPrResponses, githubIssueResponses)
            .flatMap(List::stream)
            .filter(res -> res.updatedAt().isAfter(from))
            .map(res -> LinkUpdate.builder()
                .resourceUrl(link.url())
                .title(res.title())
                .updateUrl(res.url())
                .user(res.user().login())
                .userUrl(res.user().url())
                .description(StringUtils.left(res.body(), PREVIEW_LENGTH))
                .updatedAt(res.updatedAt())
                .tgChatIds(chatIds)
                .build());
    }

    @Override
    protected LinkType getLinkType() {
        return LinkType.GITHUB;
    }

    @Override
    protected String getMessage() {
        return "Github updates";
    }
}
