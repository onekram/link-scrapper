package backend.academy.scrapper.repository.orm;

import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.orm.entity.Link;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByUrl(String url);

    @EntityGraph(attributePaths = {"subscriptions", "subscriptions.chat"})
    List<Link> findAllByType(LinkType type, Pageable pageable);
}
