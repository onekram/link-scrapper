package backend.academy.scrapper.model

data class ApiErrorResponse (
    val description: String,
    val code: String,
    val exceptionName: String,
    val exceptionMessage: String,
    val stacktrace: List<String>
)

data class LinkResponse (
    val id: Long,
    val url: String,
    val tags: List<String>,
    val filters: List<String>
)

data class ListLinksResponse (
    val links: List<LinkResponse>,
    val size: Int
)

data class AddLinkRequest (
    val link: String,
    val tags: List<String>,
    val filters: List<String>
)

data class RemoveLinkRequest (
    val link: String
)
