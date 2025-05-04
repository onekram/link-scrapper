package backend.academy.scrapper.client.github;

import backend.academy.scrapper.client.model.github.GithubResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import java.time.Instant;
import java.util.List;

@HttpExchange("/repos")
public interface GithubReposClient {

    @GetExchange("/{owner}/{repo}/issues?state=open&sort=created&direction=desc&since={since}")
    List<GithubResponse> listIssues(@PathVariable String owner, @PathVariable String repo, @PathVariable Instant since);
}
