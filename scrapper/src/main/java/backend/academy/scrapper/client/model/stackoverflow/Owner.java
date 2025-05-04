package backend.academy.scrapper.client.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Owner(
    @JsonProperty("display_name")
    String displayName,
    String link
) {
}
