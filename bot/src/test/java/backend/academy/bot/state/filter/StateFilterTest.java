package backend.academy.bot.state.filter;

import static backend.academy.bot.test.utils.TestUtil.generateHandlerContext;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.bot.state.State;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StateFilterTest {

    @ParameterizedTest(name = "Test {index}. {0} == {1} is {2}")
    @MethodSource("argumentsStream")
    @DisplayName("Happy path")
    void test(State required, State given, boolean result) {
        StateFilter stateFilter = new StateFilter(required);
        assertEquals(result, stateFilter.test(generateHandlerContext(given)));
    }

    private static Stream<Arguments> argumentsStream() {
        return Stream.of(Arguments.of(State.START, State.START, true), Arguments.of(State.START, State.MENU, false));
    }
}
