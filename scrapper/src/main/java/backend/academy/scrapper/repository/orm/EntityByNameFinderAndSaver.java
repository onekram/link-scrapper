package backend.academy.scrapper.repository.orm;

import java.util.Optional;

public interface EntityByNameFinderAndSaver<T> {
    Optional<T> findByName(String name);

    T save(T entity);
}
