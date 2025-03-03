package backend.academy.scrapper.repository

import java.net.URL

data class LinkRecord (
    val id: Long,
    val url: URL,
    val tags: List<String>,
    val filters: List<String>,
)

data class ChatRecord(
    val id: Long,
    val links: List<Long>,
)
