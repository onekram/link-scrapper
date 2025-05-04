package backend.academy.scrapper.update;

import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.client.github.GithubReposClient;
import backend.academy.scrapper.client.model.github.GithubResponse;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.entity.Link;
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
public class GithubUpdateService extends AbstractUpdateService {
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
        List<Long> chatIds = link.getTgChatIds();

        List<GithubResponse> githubIssueResponses = githubReposClient.listIssues(owner, repo, from);
        link.setUpdatedAt(githubIssueResponses.stream());

        return githubIssueResponses.stream()
            .filter(res -> res.createdAt().isAfter(from))
            .map(res -> LinkUpdate.builder()
                .resourceUrl(link.url())
                .title(res.title())
                .updateUrl(res.url())
                .user(res.user().login())
                .userUrl(res.user().url())
                .description(StringUtils.left(res.body(), PREVIEW_LENGTH))
                .time(res.createdAt())
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
