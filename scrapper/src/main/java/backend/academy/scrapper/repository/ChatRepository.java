package backend.academy.scrapper.repository;

import backend.academy.scrapper.repository.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    void deleteChatById(Long id);

    boolean existsChatById(Long id);
}
