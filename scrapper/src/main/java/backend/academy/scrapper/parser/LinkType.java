package backend.academy.scrapper.parser;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum LinkType {
    GITHUB("^(?:https?://)?"
        + "github\\.com/"
        + "([^/]+)/"
        + "([^/]+)$");

    private final Pattern pattern;

    LinkType(String regex) {
        pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public static Optional<LinkType> getType(String url) {
        return Arrays.stream(LinkType.values())
            .filter(linkType -> linkType.pattern.matcher(url).matches())
            .findFirst();
    }

    public Matcher parseUrl(String url) {
        return pattern.matcher(url);
    }
}
