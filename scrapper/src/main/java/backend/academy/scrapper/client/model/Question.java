package backend.academy.scrapper.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;

public record Question(
        @JsonProperty("question_id") Long questionId,
        String link,
        @JsonProperty("last_activity_date") @JsonDeserialize(using = UnixTimestampToInstantDeserializer.class)
                Instant lastActivityDate) {}
