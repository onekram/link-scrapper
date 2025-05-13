package backend.academy.scrapper.repository.orm;

import backend.academy.scrapper.repository.orm.entity.Chat;
import backend.academy.scrapper.repository.orm.entity.Link;
import backend.academy.scrapper.repository.orm.entity.Subscription;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByChatAndLink(Chat chat, Link link);
}
