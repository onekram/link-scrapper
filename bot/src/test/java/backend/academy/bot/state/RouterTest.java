package backend.academy.bot.state;

import static backend.academy.bot.test.utils.TestUtil.generateHandlerContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.state.handler.Handler;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RouterTest {

    @Mock
    private Handler handler1;

    @Mock
    private Handler handler2;

    @Spy
    private ArrayList<Handler> handlers;

    @InjectMocks
    private Router router;

    @BeforeEach
    public void setUp() {
        handlers.add(handler1);
        handlers.add(handler2);
    }

    @Test
    @DisplayName("First handler handle")
    void firstHandle() {
        when(handler1.handle(any())).thenReturn(true);
        when(handler1.nextState()).thenReturn(State.TRACK_FILTERS);

        verify(handler2, never()).handle(any());
        verify(handler2, never()).nextState();

        assertEquals(State.TRACK_FILTERS, router.process(generateHandlerContext()));
    }

    @Test
    @DisplayName("Second handler handle")
    void secondHandle() {
        when(handler1.handle(any())).thenReturn(false);
        verify(handler1, never()).nextState();

        when(handler2.handle(any())).thenReturn(true);
        when(handler2.nextState()).thenReturn(State.TRACK_LINK);

        assertEquals(State.TRACK_LINK, router.process(generateHandlerContext()));
    }

    @Test
    @DisplayName("No handler handle")
    void noHandle() {
        when(handler1.handle(any())).thenReturn(false);
        verify(handler1, never()).nextState();

        when(handler2.handle(any())).thenReturn(false);
        verify(handler2, never()).nextState();

        assertEquals(State.MENU, router.process(generateHandlerContext()));
    }
}
