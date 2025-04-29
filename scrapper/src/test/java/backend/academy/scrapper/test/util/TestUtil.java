package backend.academy.scrapper.test.util;

import backend.academy.scrapper.parser.LinkType;
import backend.academy.scrapper.repository.record.LinkRecord;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtil {

    @SneakyThrows
    public static LinkRecord generateLinkRecord(
            long id, String url, List<String> tags, List<String> filters, LinkType linkType) {
        return new LinkRecord(id, new URI(url), tags, filters, linkType);
    }

    public static LinkRecord generateLinkRecord(long id, String url) {
        return generateLinkRecord(id, url, null);
    }

    public static LinkRecord generateLinkRecord(long id, String url, LinkType linkType) {
        return generateLinkRecord(id, url, Collections.emptyList(), Collections.emptyList(), linkType);
    }

    public static LinkRecord generateLinkRecord(long id, String url, List<String> tags, List<String> filters) {
        return generateLinkRecord(id, url, tags, filters, null);
    }

    public static long generateLong() {
        return new Random().nextLong(1L, 1000L);
    }

    public static String createHttpAddress(String host, int port) {
        return "http://%s:%d".formatted(host, port);
    }
}
