package backend.academy.scrapper.repository.orm;

import backend.academy.scrapper.repository.orm.entity.Filter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilterRepository extends JpaRepository<Filter, Long>, EntityByNameFinderAndSaver<Filter> {}
