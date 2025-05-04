package backend.academy.scrapper.client.model.stackoverflow;

import backend.academy.scrapper.client.model.Created;

public interface Response extends Created {
    Owner owner();
    String link();
    String bodyMarkdown();
}
