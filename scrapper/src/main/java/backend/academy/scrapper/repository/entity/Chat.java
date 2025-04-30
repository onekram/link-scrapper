package backend.academy.scrapper.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Setter
@Entity
@Table(schema = "subscription")
@NoArgsConstructor
public class Chat {
    @Id
    @Column(nullable = false)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "subscription",
            schema = "subscription",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "link_id"))
    private Set<Link> links = new HashSet<>();

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Chat(Long id) {
        this.id = id;
    }
}
