package backend.academy.bot.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import backend.academy.bot.test.utils.TestUtil;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class MessageUtilTest {

    @Test
    void testNotEmpty() {
        String result = MessageUtil.linkMessage(
                TestUtil.generateLinkResponse("url", List.of("tag1", "tag2"), List.of("filter1", "filter2")));
        assertLines(3, result);
        assertThat(result).contains("url", "tag1", "tag2", "filter1", "filter2");
    }

    @Test
    void testNoTags() {
        String result = MessageUtil.linkMessage(
                TestUtil.generateLinkResponse("url", Collections.emptyList(), List.of("filter1", "filter2")));
        assertLines(2, result);
        assertThat(result).contains("url", "filter1", "filter2");
    }

    @Test
    void testNoFilters() {
        String result = MessageUtil.linkMessage(
                TestUtil.generateLinkResponse("url", List.of("tag1", "tag2"), Collections.emptyList()));
        assertLines(2, result);
        assertThat(result).contains("url", "tag1", "tag2");
    }

    @Test
    void testNoParameters() {
        String result = MessageUtil.linkMessage(TestUtil.generateLinkResponse("url"));
        assertLines(1, result);
        assertThat(result).contains("url");
    }

    private void assertLines(int count, String result) {
        assertEquals(count, result.split("\n").length);
    }
}
