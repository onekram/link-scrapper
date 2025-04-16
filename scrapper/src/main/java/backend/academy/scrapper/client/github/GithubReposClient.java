package backend.academy.scrapper.client.github;

import backend.academy.scrapper.client.model.GithubRepositoryResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/repos")
public interface GithubReposClient {

    @GetExchange("/{owner}/{repo}")
    GithubRepositoryResponse checkForUpdates(@PathVariable String owner, @PathVariable String repo);
}
