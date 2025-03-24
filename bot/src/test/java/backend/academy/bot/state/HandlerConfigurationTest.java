package backend.academy.bot.state;

import static backend.academy.bot.state.HandlerConfiguration.ADD_LINK_BUILDER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.service.ChatService;
import backend.academy.bot.service.LinksService;
import backend.academy.bot.test.utils.TestUtil;
import backend.academy.model.AddLinkRequest;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class HandlerConfigurationTest {

    @Autowired
    private Router router;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private LinksService linksService;

    @MockitoBean
    private TelegramBot telegramBot;

    @MockitoBean
    private HandlerContextParameters handlerContextParameters;

    @Mock
    private AddLinkRequest.Builder builder;

    @Autowired
    private ResourceBundle resourceBundle;

    private BaseRequest<?, ?> sendMessage;

    @BeforeEach
    public void setUp() {
        sendMessage = null;
    }

    @Test
    @DisplayName("Unrecognized answer")
    void unrecoginzedAnswer() {
        Message message = TestUtil.generateMessage("wtf", 123L);
        State currentState = State.MENU;
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.MENU, nextState);

        updateCaptor();
        checkSentMessageResourceBundle("unsupported.command.message");
        checkSentMessageChatId(123L);
        assertKeyboardContainsMenu();
    }

    @ParameterizedTest(name = "Jump to menu for {0}")
    @MethodSource("allStates")
    @DisplayName("Go to menu in any state")
    void backToMenu(State currentState) {
        Message message = TestUtil.generateMessage(resourceBundle.getString("menu.message"), 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.MENU, nextState);
        updateCaptor();
        checkSentMessageResourceBundle("available.list.of.commands.message");
        checkSentMessageChatId(123L);
        assertKeyboardIsRemoved();
    }

    private static Stream<State> allStates() {
        return Arrays.stream(State.values());
    }

    @ParameterizedTest(name = "Start handler for {0}")
    @MethodSource("allStatesExceptStart")
    @DisplayName("Jump to start handler in any state except Start")
    void startHandle(State currentState) {
        Message message = TestUtil.generateMessage("/start", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.MENU, nextState);
        updateCaptor();
        checkSentMessageResourceBundle("start.message");
        checkSentMessageChatId(123L);
        assertKeyboardIsRemoved();
    }

    private static Stream<State> allStatesExceptStart() {
        return allStates().filter(state -> !state.equals(State.START));
    }

    @Test
    @DisplayName("Welcome message for start")
    void welcomeMessage() {
        State currentState = State.START;
        Message message = TestUtil.generateMessage("/start", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.MENU, nextState);
        updateCaptor();
        checkSentMessageResourceBundle("welcome.message");
        checkSentMessageChatId(123L);
        assertKeyboardIsRemoved();
    }

    @ParameterizedTest(name = "Help handler for {0}")
    @MethodSource("allStates")
    @DisplayName("Help handler for any state")
    void helpHandler(State currentState) {
        Message message = TestUtil.generateMessage("/help", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.MENU, nextState);
        updateCaptor();
        checkSentMessageResourceBundle("available.list.of.commands.message");
        checkSentMessageChatId(123L);
        assertKeyboardIsRemoved();
    }

    @Test
    @DisplayName("Track handler")
    void trackHandler() {
        State currentState = State.MENU;
        Message message = TestUtil.generateMessage("/track", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.TRACK_LINK, nextState);
        updateCaptor();
        checkSentMessageResourceBundle("input.resource.link.message");
        checkSentMessageChatId(123L);
        assertKeyboardContainsMenu();
    }

    @Test
    @DisplayName("Track link handler")
    void trackLinkHandler() {
        State currentState = State.TRACK_LINK;
        Message message = TestUtil.generateMessage("url", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.TRACK_TAGS, nextState);
        updateCaptor();

        ArgumentCaptor<AddLinkRequest.Builder> captor = ArgumentCaptor.forClass(AddLinkRequest.Builder.class);
        verify(handlerContextParameters, times(1)).setParameter(eq(ADD_LINK_BUILDER), captor.capture());
        assertEquals("url", captor.getValue().build().getLink());
        checkSentMessageResourceBundle("input.tags.message");
        checkSentMessageChatId(123L);
        assertKeyboardContainsMenu();
        assertKeyboardContainsText("Work", "Study");
    }

    @Test
    @DisplayName("Track tags handler")
    void trackTagsHandler() {
        when(handlerContextParameters.getParameter(ADD_LINK_BUILDER, AddLinkRequest.Builder.class))
                .thenReturn(builder);
        State currentState = State.TRACK_TAGS;
        Message message = TestUtil.generateMessage("  tag1   tag2 ", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.TRACK_FILTERS, nextState);
        updateCaptor();
        verify(handlerContextParameters, times(1)).getParameter(ADD_LINK_BUILDER, AddLinkRequest.Builder.class);
        verify(builder, times(1)).tags(List.of("tag1", "tag2"));
        checkSentMessageResourceBundle("input.filters.message");
        checkSentMessageChatId(123L);
        assertKeyboardContainsMenu();
    }

    @Test
    @DisplayName("Track filters handler")
    void trackFiltersHandler() {
        when(handlerContextParameters.getParameter(ADD_LINK_BUILDER, AddLinkRequest.Builder.class))
                .thenReturn(builder);
        AddLinkRequest request = new AddLinkRequest.Builder().link("url").build();
        when(builder.build()).thenReturn(request);
        State currentState = State.TRACK_FILTERS;
        Message message = TestUtil.generateMessage("  filter1   filter2  ", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.MENU, nextState);
        updateCaptor();
        verify(handlerContextParameters, times(1)).getParameter(ADD_LINK_BUILDER, AddLinkRequest.Builder.class);
        verify(builder, times(1)).filters(List.of("filter1", "filter2"));
        verify(builder, times(1)).build();
        verify(linksService, times(1)).trackLink(123L, request);
        checkSentMessageResourceBundle("saved.message");
        checkSentMessageChatId(123L);
        assertKeyboardIsRemoved();
    }

    @Test
    @DisplayName("List of links handler")
    void listHandler() {
        when(linksService.getTrackedLinks(123L))
                .thenReturn(new ListLinksResponse(
                        List.of(TestUtil.generateLinkResponse("first"), TestUtil.generateLinkResponse("second")), 2));
        State currentState = State.MENU;
        Message message = TestUtil.generateMessage("/list", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.MENU, nextState);
        verify(linksService, times(1)).getTrackedLinks(123L);
        updateCaptor();
        checkSentMessageContains("first", "second");
        checkSentMessageChatId(123L);
        assertKeyboardIsRemoved();
    }

    @Test
    @DisplayName("Empty list of links")
    void emptyList() {
        when(linksService.getTrackedLinks(123L)).thenReturn(new ListLinksResponse(Collections.emptyList(), 0));
        State currentState = State.MENU;
        Message message = TestUtil.generateMessage("/list", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.MENU, nextState);
        verify(linksService, times(1)).getTrackedLinks(123L);
        updateCaptor();
        checkSentMessageResourceBundle("no.links.message");
        checkSentMessageChatId(123L);
        assertKeyboardIsRemoved();
    }

    @Test
    @DisplayName("Untrack handler")
    void untrackHandler() {
        when(linksService.getTrackedLinks(123L))
                .thenReturn(new ListLinksResponse(
                        List.of(
                                TestUtil.generateLinkResponse("first-url"),
                                TestUtil.generateLinkResponse("second-url")),
                        2));
        State currentState = State.MENU;
        Message message = TestUtil.generateMessage("/untrack", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.UNTRACK_LINK, nextState);
        verify(linksService, times(1)).getTrackedLinks(123L);
        updateCaptor();
        checkSentMessageResourceBundle("untrack.links.message");
        checkSentMessageChatId(123L);
        assertKeyboardContainsMenu();
        assertKeyboardContainsText("first-url", "second-url");
    }

    @Test
    @DisplayName("Input link to untrack handler")
    void inputLinkToUntrackHandler() {
        when(linksService.untrackLink(eq(123L), any(RemoveLinkRequest.class)))
                .thenReturn(TestUtil.generateLinkResponse("first-url"));

        State currentState = State.UNTRACK_LINK;
        Message message = TestUtil.generateMessage("url", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.MENU, nextState);
        verify(linksService, times(1)).untrackLink(eq(123L), any(RemoveLinkRequest.class));
        updateCaptor();
        checkSentMessageContains(resourceBundle.getString("unsubscribed.message"), "first-url");
        checkSentMessageChatId(123L);
        assertKeyboardIsRemoved();
    }

    private void checkSentMessageResourceBundle(String key) {
        assertEquals(resourceBundle.getString(key), TestUtil.getText(sendMessage));
    }

    private void checkSentMessageChatId(long id) {
        assertEquals(id, TestUtil.getId(sendMessage));
    }

    private void checkSentMessageContains(String... text) {
        for (String s : text) {
            assertThat(TestUtil.getText(sendMessage), containsString(s));
        }
    }

    private void updateCaptor() {
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBot, times(1)).execute(captor.capture());
        sendMessage = captor.getValue();
    }

    private void assertKeyboardIsRemoved() {
        TestUtil.assertKeyboardIsRemoved(sendMessage);
    }

    private void assertKeyboardContainsMenu() {
        assertKeyboardContainsText("Menu");
    }

    private void assertKeyboardContainsText(String... texts) {
        TestUtil.assertKeyboardIsReplyMarkup(sendMessage);
        TestUtil.assertContainsButton(TestUtil.getKeyboard(sendMessage), texts);
    }
}
