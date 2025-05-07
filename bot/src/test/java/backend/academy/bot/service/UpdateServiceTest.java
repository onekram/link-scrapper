package backend.academy.bot.service;

import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.Router;
import backend.academy.bot.state.State;
import backend.academy.bot.test.utils.TestUtil;
import backend.academy.bot.util.LogUtil;
import ch.qos.logback.classic.Level;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.ResourceBundle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static backend.academy.bot.test.utils.LoggerTestUtil.appenderContainsLog;
import static backend.academy.bot.test.utils.LoggerTestUtil.getListAppender;
import static backend.academy.bot.test.utils.TestUtil.generateCallbackQuery;
import static backend.academy.bot.test.utils.TestUtil.generateLinkUpdate;
import static backend.academy.bot.test.utils.TestUtil.generateMessage;
import static backend.academy.bot.test.utils.TestUtil.generateUpdate;
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
    private StateService stateService;

    @Mock
    private ResourceBundle resourceBundle;

    @InjectMocks
    private UpdateService updateService;

    @Nested
    class UpdatesFromTelegram {
        @Test
        @DisplayName("Callback received")
        void doNothingOnNull() {
            var appender = getListAppender(LogUtil.class);

            String data = "delete_subscription:url";
            Update update = generateUpdate(generateCallbackQuery(123L, data));

            State state = State.TRACK_FILTERS;
            when(stateService.getState(anyLong())).thenReturn(state);
            when(router.process(any())).thenReturn(state);

            updateService.updateProcess(update);

            verify(stateService, times(1)).getState(any());
            verify(stateService, times(1)).setState(any(), any());
            verify(router, times(1)).process(any());
            verify(telegramBot, never()).execute(any());

            assertTrue(appenderContainsLog(
                appender,
                Level.INFO,
                "Message chatId: %s, current state: %s, callback data: %s".formatted(123L, state, data)));
        }

        @Test
        @DisplayName("Happy path")
        void happyPath() {
            var appender = getListAppender(LogUtil.class);

            Message message = generateMessage("text", 123L);
            Update update = generateUpdate(message);

            State state = State.TRACK_FILTERS;
            State nextState = State.MENU;
            when(stateService.getState(anyLong())).thenReturn(state);
            when(router.process(any())).thenReturn(nextState);

            updateService.updateProcess(update);

            verify(router).process(new HandlerContext(message, telegramBot, state));
            verify(stateService).setState(123L, nextState);
            verify(telegramBot, never()).execute(any());

            assertTrue(appenderContainsLog(
                    appender,
                    Level.INFO,
                    "Message chatId: %s, current state: %s, text: %s".formatted(123L, state, message.text())));
        }

        @Test
        @DisplayName("Router throw Exception")
        void exception() {
            var appender = getListAppender(UpdateService.class);

            Message message = generateMessage("text", 123L);
            Update update = generateUpdate(message);

            State state = State.TRACK_FILTERS;
            when(stateService.getState(anyLong())).thenReturn(state);
            when(router.process(any())).thenThrow(new RuntimeException("exception"));
            when(resourceBundle.getString("error.message")).thenReturn("Error");

            updateService.updateProcess(update);

            ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
            verify(stateService, times(1)).setState(123L, State.MENU);
            verify(telegramBot, times(1)).execute(argumentCaptor.capture());
            SendMessage sendMessage = argumentCaptor.getValue();
            assertEquals("Error", TestUtil.getText(sendMessage));

            assertTrue(appenderContainsLog(appender, Level.ERROR, "Exception while routing occurred"));
        }
    }

    @Nested
    @DisplayName("Update send update for received linkUpdate message")
    class UpdatesFromScrapper {
        @Test
        @DisplayName("Updates for links 1, 2, 3, 4")
        void happyPath() {
            var linkUpdate = generateLinkUpdate(1L, 2L, 3L, 4L);
            when(resourceBundle.getString("update.format.message")).thenReturn("%s and %s");
            updateService.updateProcess(linkUpdate);

            ArgumentCaptor<SendMessage> argumentCaptor = ArgumentCaptor.forClass(SendMessage.class);
            verify(telegramBot, times(4)).execute(argumentCaptor.capture(), any());

            var values = argumentCaptor.getAllValues().iterator();
            for (var i : List.of(1L, 2L, 3L, 4L)) {
                SendMessage sendMessage = values.next();
                assertEquals("title and resourceUrl", TestUtil.getText(sendMessage));
                assertEquals(i, TestUtil.getId(sendMessage));
            }
        }
    }
}
