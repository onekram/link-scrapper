package backend.academy.scrapper.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Matcher;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LinkTypeTest {

    @Test
    void getType() {
        assertEquals(
                LinkType.GITHUB,
                LinkType.getType("https://github.com/onekram/game").orElseThrow());
        assertEquals(
                LinkType.GITHUB, LinkType.getType("https://github.com/who/repo").orElseThrow());
        assertEquals(
                LinkType.GITHUB, LinkType.getType("http://github.com/who/repo").orElseThrow());
        assertNull(LinkType.getType("https://githsdfsub.com/who/repo").orElse(null));
    }

    @ParameterizedTest(name = "{index}: url: {0}, owner: {1}, repo: {2}")
    @MethodSource("methodSource")
    void parseUrl(String url, String owner, String repo) {
        Matcher matcher = LinkType.GITHUB.parseUrl(url);
        assertTrue(matcher.matches());
        assertEquals(owner, matcher.group(1));
        assertEquals(repo, matcher.group(2));
    }

    private static Stream<Arguments> methodSource() {
        return Stream.of(
                Arguments.of("https://github.com/onekram/game", "onekram", "game"),
                Arguments.of("https://github.com/onekram/factorization", "onekram", "factorization"));
    }
}
