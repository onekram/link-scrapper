package backend.academy.scrapper.client.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;
import java.util.List;

public record Answer(
        @JsonSetter(nulls = Nulls.AS_EMPTY) List<Comment> comments,
        Owner owner,
        String link,
        @JsonProperty("body_markdown") String bodyMarkdown,
        @JsonProperty("creation_date") @JsonDeserialize(using = UnixTimestampToInstantDeserializer.class)
                Instant createdAt)
        implements Response {}
