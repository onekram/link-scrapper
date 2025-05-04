package backend.academy.scrapper.client.github;

import backend.academy.scrapper.client.model.github.GithubResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import java.util.List;

@HttpExchange("/repos")
public interface GithubReposClient {

    @GetExchange("/{owner}/{repo}/pulls?state=open&sort=created&direction=desc")
    List<GithubResponse> listPulls(@PathVariable String owner, @PathVariable String repo);

    @GetExchange("/{owner}/{repo}/issues?state=open")
    List<GithubResponse> listIssues(@PathVariable String owner, @PathVariable String repo);
}
