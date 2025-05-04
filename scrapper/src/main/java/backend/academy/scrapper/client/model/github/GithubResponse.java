package backend.academy.scrapper.client.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record GithubResponse(
    String title,
    @JsonProperty("html_url")
    String url,
    GithubUser user,
    @JsonProperty("updated_at")
    Instant updatedAt,
    String body
) {}
