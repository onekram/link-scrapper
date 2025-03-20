package backend.academy.bot.state.filter;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import static backend.academy.bot.state.TestUtil.generateHandlerContext;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class MessageTextFilterTest {

    @ParameterizedTest(name = "Test {index}. {0} == {1} is {2}")
    @MethodSource("argumentsStream")
    @DisplayName("Happy path")
    void test(String required, String given, boolean result) {
        MessageTextFilter messageTextFilter = new MessageTextFilter(required);
        assertEquals(result, messageTextFilter.test(generateHandlerContext(given)));
    }

    private static Stream<Arguments> argumentsStream() {
        return Stream.of(
            Arguments.of("text", "text", true),
            Arguments.of("text", "TeXt", true),
            Arguments.of("sdlfj", "text", false)
        );
    }
}
