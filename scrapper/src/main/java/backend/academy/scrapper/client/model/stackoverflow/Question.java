package backend.academy.scrapper.client.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;
import java.util.List;

public record Question(
        List<Comment> comments,
        List<Answer> answers,
        @JsonProperty("question_id") Long questionId,
        Owner owner,
        String link,
        String title,
        @JsonProperty("creation_date") @JsonDeserialize(using = UnixTimestampToInstantDeserializer.class)
                Instant createdAt,
        @JsonProperty("body_markdown") String bodyMarkdown)
        implements Response {}
