package backend.academy.bot.state.filter;

import backend.academy.bot.state.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;
import static backend.academy.bot.state.TestUtil.generateHandlerContext;
import static org.junit.jupiter.api.Assertions.*;

class StateFilterTest {

    @ParameterizedTest(name = "Test {index}. {0} == {1} is {2}")
    @MethodSource("argumentsStream")
    @DisplayName("Happy path")
    void test(State required, State given, boolean result) {
        StateFilter stateFilter = new StateFilter(required);
        assertEquals(result, stateFilter.test(generateHandlerContext(given)));
    }

    private static Stream<Arguments> argumentsStream() {
        return Stream.of(
            Arguments.of(State.START, State.START, true),
            Arguments.of(State.START, State.MENU, false)
        );
    }
}
