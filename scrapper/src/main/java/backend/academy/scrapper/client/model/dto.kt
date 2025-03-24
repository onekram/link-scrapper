package backend.academy.scrapper.client.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
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

class UnixTimestampToInstantDeserializer : JsonDeserializer<Instant>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Instant {
        return Instant.ofEpochSecond(p.valueAsLong)
    }
}

data class Question(
    @JsonProperty("question_id")
    val questionId: Long,
    val link: String,
    @JsonProperty("last_activity_date")
    @JsonDeserialize(using = UnixTimestampToInstantDeserializer::class)
    val lastActivityDate: Instant
)

data class StackOverflowQuestionsResponse(
    val items: List<Question>
)

data class StackOverflowApiErrorResponse(
    @JsonProperty("error_id")
    val errorId: Long,
    @JsonProperty("error_message")
    val errorMessage: String,
    @JsonProperty("error_name")
    val errorName: String
)
