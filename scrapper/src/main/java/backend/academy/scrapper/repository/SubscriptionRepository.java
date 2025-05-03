package backend.academy.scrapper.repository;

import backend.academy.scrapper.repository.entity.Chat;
import backend.academy.scrapper.repository.entity.Link;
import backend.academy.scrapper.repository.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByChatAndLink(Chat chat, Link link);
}
