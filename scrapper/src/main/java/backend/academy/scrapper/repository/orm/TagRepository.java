package backend.academy.scrapper.repository.orm;

import backend.academy.scrapper.repository.orm.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, EntityByNameFinderAndSaver<Tag> {}
