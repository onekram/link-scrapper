package backend.academy.scrapper.client.bot;

import backend.academy.model.LinkUpdate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/updates")
public interface UpdatesClient {

    @PostExchange
    void updates(@RequestBody LinkUpdate request);
}
