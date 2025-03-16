package backend.academy.model

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
) {
    class Builder {
        private var link: String = ""
        private var tags: List<String> = emptyList()
        private var filters: List<String> = emptyList()

        fun link(link: String) = apply { this.link = link }
        fun tags(tags: List<String>) = apply { this.tags = tags }
        fun filters(filters: List<String>) = apply { this.filters = filters }

        fun build(): AddLinkRequest {
            return AddLinkRequest(link, tags, filters)
        }
    }
}

data class RemoveLinkRequest (
    val link: String
)
