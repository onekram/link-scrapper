package backend.academy.scrapper.repository;

import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.entity.Link;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {
    List<Link> findAllByType(LinkType type);
}
