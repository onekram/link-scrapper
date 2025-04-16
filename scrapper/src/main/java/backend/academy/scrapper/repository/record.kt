package backend.academy.scrapper.repository

import backend.academy.scrapper.parser.LinkType
import java.net.URI

data class LinkRecord (
    val id: Long,
    val url: URI,
    val tags: List<String>,
    val filters: List<String>,
    val type: LinkType?
)

data class ChatRecord(
    val id: Long,
    val links: List<Long>,
)
