package backend.academy.scrapper.test.util;

import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.scrapper.repository.entity.Chat;
import backend.academy.scrapper.repository.entity.Link;
import backend.academy.scrapper.repository.entity.Subscription;
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
        Link link = new Link(url);
        link.subscriptions(Arrays.stream(chats).mapToObj(id -> new Subscription(new Chat(id), link)).collect(Collectors.toSet()));
        return link;
    }

    public static Chat generateChat(long id, String... urls) {
        Chat chat = new Chat(id);
        chat.subscriptions(Arrays.stream(urls).map(url -> new Subscription(chat, generateLink(url, id))).collect(Collectors.toSet()));
        return chat;
    }
}
