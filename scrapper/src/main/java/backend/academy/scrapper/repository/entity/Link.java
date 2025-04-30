package backend.academy.scrapper.repository.entity;

import backend.academy.scrapper.parser.LinkType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(schema = "subscription")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    private String url;

    @ManyToMany
    @JoinTable(
            name = "link_tag",
            schema = "subscription",
            joinColumns = @JoinColumn(name = "link_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "link_filter",
            schema = "subscription",
            joinColumns = @JoinColumn(name = "link_id"),
            inverseJoinColumns = @JoinColumn(name = "filter_id"))
    private Set<Filter> filters = new HashSet<>();

    @ManyToMany(mappedBy = "links")
    private Set<Chat> chats = new HashSet<>();

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "subscription.link_type")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private LinkType type;

    public Link(String url, Set<Tag> tags, Set<Filter> filters) {
        this.url = url;
        this.tags = tags;
        this.filters = filters;
        this.type = LinkType.getType(url).orElse(null);
    }
}
