package backend.academy.scrapper.repository;

import java.util.Optional;

public interface EntityByNameFinderAndSaver<T> {
    Optional<T> findByName(String name);
    T save(T entity);
}
