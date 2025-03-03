package backend.academy.bot.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/tg-chat")
public interface ChatClient {

    @PostExchange("/{id}")
    void registerChat(@PathVariable Long id);

    @DeleteExchange("/{id}")
    void unRegisterChat(@PathVariable Long id);
}
