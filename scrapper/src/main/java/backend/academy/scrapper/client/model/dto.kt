package backend.academy.scrapper.client.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

data class GithubRepositoryResponse(
    @JsonProperty("updated_at")
    val lastUpdate: Instant
)

data class GithubApiError (
    val message: String,
    val documentationURL: String,
    val status: String
)
