package backend.academy.scrapper.update;

import backend.academy.scrapper.client.github.GithubReposClient;
import backend.academy.scrapper.client.model.GithubRepositoryResponse;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.entity.Link;
import backend.academy.scrapper.service.LinksService;
import java.time.Instant;
import java.util.regex.Matcher;
import org.springframework.stereotype.Service;

@Service
public class GithubUpdateService extends AbstractUpdateService {
    private final GithubReposClient githubReposClient;

    public GithubUpdateService(LinksService linksService, GithubReposClient githubReposClient) {
        super(linksService);
        this.githubReposClient = githubReposClient;
    }

    @Override
    protected boolean isUpdated(Link link, Instant from) {
        Matcher matcher = LinkType.GITHUB.parseUrl(link.url());
        if (!matcher.matches()) {
            throw new IllegalStateException("Link of GITHUB type doesn't match pattern");
        }
        String owner = matcher.group(1);
        String repo = matcher.group(2);
        GithubRepositoryResponse response = githubReposClient.checkForUpdates(owner, repo);
        return response.lastUpdate().isAfter(from);
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
