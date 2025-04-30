package backend.academy.scrapper.test.util;

import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.scrapper.repository.entity.Chat;
import backend.academy.scrapper.repository.entity.Link;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtil {

    public static String createHttpAddress(String host, int port) {
        return "http://%s:%d".formatted(host, port);
    }

    public static LinkResponse generateLinkResponse(long id, String url) {
        return new LinkResponse(id, url, Collections.emptyList(), Collections.emptyList());
    }

    @SafeVarargs
    public static ListLinksResponse generateListLinksResponse(Map.Entry<Long, String>... entry) {
        List<LinkResponse> linkResponses = Arrays.stream(entry)
                .map(e -> generateLinkResponse(e.getKey(), e.getValue()))
                .toList();
        return new ListLinksResponse(linkResponses, linkResponses.size());
    }

    public static Link generateLink(String url, long... chats) {
        return new Link(url, Collections.emptySet(), Collections.emptySet())
                .chats(Arrays.stream(chats).mapToObj(id -> new Chat().id(id)).collect(Collectors.toSet()));
    }

    public static Chat generateChat(long id, String... urls) {
        return new Chat(id)
                .links(Arrays.stream(urls).map(url -> generateLink(url, id)).collect(Collectors.toSet()));
    }
}
