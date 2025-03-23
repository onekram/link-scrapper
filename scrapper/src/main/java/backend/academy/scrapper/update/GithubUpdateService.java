package backend.academy.scrapper.update;

import backend.academy.model.LinkUpdate;
import backend.academy.scrapper.client.github.GithubReposClient;
import backend.academy.scrapper.client.model.RepositoryResponse;
import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.LinkRecord;
import backend.academy.scrapper.service.LinksService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GithubUpdateService {
    private final LinksService linksService;
    private final GithubReposClient githubReposClient;


    public List<LinkUpdate> getUpdates(Instant from) {
        return linksService.fetchIdAndLinksByType(LinkType.GITHUB).entrySet().stream()
            .flatMap(entry -> entry.getValue().stream()
                .map(link -> Map.entry(entry.getKey(), link)))
            .filter(entry -> isUpdated(entry.getValue(), from))
            .collect(Collectors.groupingBy(entry -> entry.getValue().getUrl().toString(),
                Collectors.mapping(Map.Entry::getKey, Collectors.toList()))).entrySet().stream()
            .map(entry -> new LinkUpdate(System.currentTimeMillis(), entry.getKey(), "Github updates", entry.getValue()))
            .toList();
    }

    private boolean isUpdated(LinkRecord linkRecord, Instant from) {
        Matcher matcher = LinkType.GITHUB.parseUrl(linkRecord.getUrl().toString());
        if (!matcher.matches()) {
            throw new IllegalStateException("Link of GITHUB type doesn't match pattern");
        }
        String owner = matcher.group(1);
        String repo = matcher.group(2);
        RepositoryResponse response = githubReposClient.checkForUpdates(owner, repo);
        return response.getLastUpdate().isAfter(from);
    }
}
