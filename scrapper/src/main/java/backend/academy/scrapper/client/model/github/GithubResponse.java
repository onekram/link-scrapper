package backend.academy.scrapper.client.model.github;

import backend.academy.scrapper.client.model.Created;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record GithubResponse(
        String title,
        @JsonProperty("html_url") String url,
        GithubUser user,
        @JsonProperty("created_at") Instant createdAt,
        String body)
        implements Created {}
