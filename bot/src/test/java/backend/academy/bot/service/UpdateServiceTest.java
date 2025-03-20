package backend.academy.bot.service;

import backend.academy.bot.repository.state.StateRepository;
import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.Router;
import backend.academy.bot.state.State;
import ch.qos.logback.classic.Level;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.ResourceBundle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static backend.academy.bot.util.LoggerTestUtil.appenderContainsLog;
import static backend.academy.bot.util.LoggerTestUtil.getListAppender;
import static backend.academy.bot.util.TestUtil.generateMessage;
import static backend.academy.bot.util.TestUtil.generateUpdate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateServiceTest {

    @Mock
    private TelegramBot telegramBot;

    @Mock
    private Router router;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private ResourceBundle resourceBundle;

    @InjectMocks
    private UpdateService updateService;

    @Test
    @DisplayName("Do nothing on null")
    void doNothingOnNull() {
        Update update = generateUpdate(null);

        updateService.updateProcess(update);

        verify(stateRepository, never()).getCurrentState(any());
        verify(stateRepository, never()).saveState(any(), any());
        verify(router, never()).process(any());
        verify(telegramBot, never()).execute(any());
    }

    @Test
    @DisplayName("Happy path")
    void happyPath() {
        var appender = getListAppender(UpdateService.class);

        Message message = generateMessage("text", 123L);
        Update update = generateUpdate(message);

        State state = State.TRACK_FILTERS;
        State nextState = State.MENU;
        when(stateRepository.getCurrentState(anyLong())).thenReturn(state);
        when(router.process(any())).thenReturn(nextState);

        updateService.updateProcess(update);

        verify(router).process(new HandlerContext(message, telegramBot, state));
        verify(stateRepository).saveState(123L, nextState);
        verify(telegramBot, never()).execute(any());

        assertTrue(appenderContainsLog(appender, Level.INFO, "Message chatId: %s, current state: %s, text: %s".formatted(123L, state, message.text())));
    }

    @Test
    @DisplayName("Happy path")
    void exception() {
        var appender = getListAppender(UpdateService.class);

        Message message = generateMessage("text", 123L);
        Update update = generateUpdate(message);

        State state = State.TRACK_FILTERS;
        when(stateRepository.getCurrentState(anyLong())).thenReturn(state);
        when(router.process(any())).thenThrow(new RuntimeException("exception"));
        when(resourceBundle.getString("error.message")).thenReturn("Error");

        updateService.updateProcess(update);

        ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(stateRepository, times(1)).saveState(123L, State.MENU);
        verify(telegramBot, times(1)).execute(argumentCaptor.capture());
        SendMessage sendMessage = argumentCaptor.getValue();
        assertEquals("Error", sendMessage.getParameters().get("text"));

        assertTrue(appenderContainsLog(appender, Level.ERROR, "Exception while routing occurred"));
    }
}
