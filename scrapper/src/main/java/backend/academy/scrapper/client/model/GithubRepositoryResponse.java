package backend.academy.scrapper.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record GithubRepositoryResponse(@JsonProperty("updated_at") Instant lastUpdate) {}
