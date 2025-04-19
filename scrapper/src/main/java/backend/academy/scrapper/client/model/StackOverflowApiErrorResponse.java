package backend.academy.scrapper.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record StackOverflowApiErrorResponse(
        @JsonProperty("error_id") Long errorId,
        @JsonProperty("error_message") String errorMessage,
        @JsonProperty("error_name") String errorName) {}
