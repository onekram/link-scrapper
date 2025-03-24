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
    void getTypeGithub() {
        assertEquals(
                LinkType.GITHUB,
                LinkType.getType("https://github.com/onekram/game").orElseThrow());

        assertEquals(
            LinkType.GITHUB,
            LinkType.getType("https://github.com/who/repo").orElseThrow());

        assertEquals(LinkType.GITHUB,
            LinkType.getType("http://github.com/who/repo").orElseThrow());

        assertNull(LinkType.getType("https://githsdfsub.com/who/repo").orElse(null));
    }

    @Test
    void getTypeStackOverflow() {
        assertEquals(LinkType.STACK_OVERFLOW,
            LinkType.getType("https://stackoverflow.com/questions/79530792/typeerror-in-fastapi-when-using-apiroute-with-a-router").orElseThrow());

        assertEquals(LinkType.STACK_OVERFLOW,
            LinkType.getType("https://stackoverflow.com/questions/79530792").orElseThrow());

        assertNull(LinkType.getType("https://stackoverflow.com/questons/79530792").orElse(null));
    }

    @ParameterizedTest(name = "{index}: url: {0}, owner: {1}, repo: {2}")
    @MethodSource("methodSourceGithub")
    void parseUrlGithub(String url, String owner, String repo) {
        Matcher matcher = LinkType.GITHUB.parseUrl(url);
        assertTrue(matcher.matches());
        assertEquals(owner, matcher.group(1));
        assertEquals(repo, matcher.group(2));
    }

    @ParameterizedTest(name = "{index}: url: {0}, owner: {1}, repo: {2}")
    @MethodSource("methodSourceStackOverflow")
    void parseUrlStackOverflow(String url, String id) {
        Matcher matcher = LinkType.STACK_OVERFLOW.parseUrl(url);
        assertTrue(matcher.matches());
        assertEquals(id, matcher.group(1));
    }

    private static Stream<Arguments> methodSourceGithub() {
        return Stream.of(
                Arguments.of("https://github.com/onekram/game", "onekram", "game"),
                Arguments.of("https://github.com/onekram/factorization", "onekram", "factorization"));
    }

    private static Stream<Arguments> methodSourceStackOverflow() {
        return Stream.of(
            Arguments.of("https://stackoverflow.com/questions/79530792/typeerror-in-fastapi-when-using-apiroute-with-a-router", "79530792"),
            Arguments.of("https://stackoverflow.com/questions/123", "123"));
    }
}
