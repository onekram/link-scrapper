package backend.academy.scrapper.repository.orm.entity;

import backend.academy.scrapper.client.model.Created;
import backend.academy.scrapper.parser.LinkType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
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

    @Column(nullable = false, unique = true)
    private String url;

    @OneToMany(mappedBy = "link", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Subscription> subscriptions = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "subscription.link_type")
    @JdbcType(PostgreSQLEnumJdbcType.class)
    private LinkType type;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column
    private Instant updatedAt = Instant.now();

    public Link(String url) {
        this.url = url;
        this.type = LinkType.getType(url).orElse(null);
    }

    public List<Long> getTgChatIds() {
        return subscriptions().stream().map(Subscription::chat).map(Chat::id).toList();
    }

    public void setUpdatedAt(Stream<? extends Created> createdStream) {
        updatedAt(createdStream
                .map(Created::createdAt)
                .max(Comparator.naturalOrder())
                .orElse(updatedAt()));
    }
}
