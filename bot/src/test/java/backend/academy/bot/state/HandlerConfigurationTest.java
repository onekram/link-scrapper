package backend.academy.bot.state;

import static backend.academy.bot.test.utils.TestUtil.generateCallbackQuery;
import static backend.academy.bot.test.utils.TestUtil.generateLinkResponse;
import static backend.academy.bot.test.utils.TestUtil.generateMessage;
import static backend.academy.bot.test.utils.TestUtil.generateUpdate;
import static backend.academy.bot.test.utils.TestUtil.getId;
import static backend.academy.bot.test.utils.TestUtil.getText;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.bot.BotConfig;
import backend.academy.bot.configuration.BeanConfiguration;
import backend.academy.bot.repository.parameters.ContextRepository;
import backend.academy.bot.service.ChatService;
import backend.academy.bot.service.LinksService;
import backend.academy.bot.test.utils.TestUtil;
import backend.academy.model.AddLinkRequest;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoBeans;

@SpringBootTest(classes = {Router.class})
@Import({HandlerConfiguration.class, BeanConfiguration.class})
@MockitoBeans({@MockitoBean(types = BotConfig.class)})
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
    private ContextRepository contextRepository;

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
        Message message = generateMessage("wtf", 123L);
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
        Message message = generateMessage(resourceBundle.getString("menu.message"), 123L);
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
        Message message = generateMessage("/start", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.MENU, nextState);
        verify(chatService, times(1)).registerChat(123L);
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
        Message message = generateMessage("/start", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.MENU, nextState);
        verify(chatService, times(1)).registerChat(123L);
        updateCaptor();
        checkSentMessageResourceBundle("welcome.message");
        checkSentMessageChatId(123L);
        assertKeyboardIsRemoved();
    }

    @ParameterizedTest(name = "Help handler for {0}")
    @MethodSource("allStates")
    @DisplayName("Help handler for any state")
    void helpHandler(State currentState) {
        Message message = generateMessage("/help", 123L);
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
        Message message = generateMessage("/track", 123L);
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
        Message message = generateMessage("resourceUrl", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.TRACK_TAGS, nextState);
        updateCaptor();

        ArgumentCaptor<AddLinkRequest.Builder> captor = ArgumentCaptor.forClass(AddLinkRequest.Builder.class);
        verify(contextRepository, times(1)).setContext(eq(123L), captor.capture());
        assertEquals("resourceUrl", captor.getValue().build().link());
        checkSentMessageResourceBundle("input.tags.message");
        checkSentMessageChatId(123L);
        assertKeyboardContainsMenu();
        assertKeyboardContainsText("Work", "Study");
    }

    @Test
    @DisplayName("Track tags handler")
    void trackTagsHandler() {
        when(contextRepository.getContext(123L, AddLinkRequest.Builder.class)).thenReturn(Optional.of(builder));
        State currentState = State.TRACK_TAGS;
        Message message = generateMessage("  tag1   tag2 ", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.TRACK_FILTERS, nextState);
        updateCaptor();
        verify(contextRepository, times(1)).getContext(123L, AddLinkRequest.Builder.class);
        verify(builder, times(1)).tags(List.of("tag1", "tag2"));
        checkSentMessageResourceBundle("input.filters.message");
        checkSentMessageChatId(123L);
        assertKeyboardContainsMenu();
    }

    @Test
    @DisplayName("Track filters handler")
    void trackFiltersHandler() {
        when(contextRepository.getContext(123L, AddLinkRequest.Builder.class)).thenReturn(Optional.of(builder));
        AddLinkRequest request = AddLinkRequest.builder().link("resourceUrl").build();
        when(builder.build()).thenReturn(request);
        State currentState = State.TRACK_FILTERS;
        Message message = generateMessage("  filter1   filter2  ", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.MENU, nextState);
        updateCaptor();
        verify(contextRepository, times(1)).getContext(123L, AddLinkRequest.Builder.class);
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
                        List.of(generateLinkResponse("first"), generateLinkResponse("second")), 2));
        State currentState = State.MENU;
        Message message = generateMessage("/list", 123L);
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
        Message message = generateMessage("/list", 123L);
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
                        List.of(generateLinkResponse("first-resourceUrl"), generateLinkResponse("second-resourceUrl")),
                        2));
        State currentState = State.MENU;
        Message message = generateMessage("/untrack", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.UNTRACK_LINK, nextState);
        verify(linksService, times(1)).getTrackedLinks(123L);
        updateCaptor();
        checkSentMessageResourceBundle("untrack.links.message");
        checkSentMessageChatId(123L);
        assertKeyboardContainsMenu();
        assertKeyboardContainsText("first-resourceUrl", "second-resourceUrl");
    }

    @Test
    @DisplayName("Input link to untrack handler")
    void inputLinkToUntrackHandler() {
        when(linksService.untrackLink(eq(123L), any(RemoveLinkRequest.class)))
                .thenReturn(generateLinkResponse("first-resourceUrl"));

        State currentState = State.UNTRACK_LINK;
        Message message = generateMessage("resourceUrl", 123L);
        HandlerContext handlerContext = new HandlerContext(message, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.MENU, nextState);
        verify(linksService, times(1)).untrackLink(eq(123L), any(RemoveLinkRequest.class));
        updateCaptor();
        checkSentMessageContains(resourceBundle.getString("unsubscribed.message"), "first-resourceUrl");
        checkSentMessageChatId(123L);
        assertKeyboardIsRemoved();
    }

    @Test
    @DisplayName("Delete subscription callback")
    void deleteSubscriptionCallbackHandler() {
        when(linksService.untrackLink(eq(123L), any(RemoveLinkRequest.class))).thenReturn(generateLinkResponse("url"));

        State currentState = State.UNTRACK_LINK;
        Update update = generateUpdate(generateCallbackQuery(123L, "delete_subscription:url"));
        HandlerContext handlerContext = new HandlerContext(update, telegramBot, currentState);

        State nextState = router.process(handlerContext);

        assertEquals(State.UNTRACK_LINK, nextState);
        ArgumentCaptor<RemoveLinkRequest> captor = ArgumentCaptor.forClass(RemoveLinkRequest.class);
        verify(linksService, times(1)).untrackLink(eq(123L), captor.capture());
        assertEquals("url", captor.getValue().link());
        verify(telegramBot, times(1)).execute(any(AnswerCallbackQuery.class));
        updateCaptor();
    }

    private void checkSentMessageResourceBundle(String key) {
        assertEquals(resourceBundle.getString(key), getText(sendMessage));
    }

    private void checkSentMessageChatId(long id) {
        assertEquals(id, getId(sendMessage));
    }

    private void checkSentMessageContains(String... text) {
        assertThat(getText(sendMessage)).contains(text);
    }

    private void updateCaptor() {
        ArgumentCaptor<BaseRequest<?, ?>> captor = ArgumentCaptor.forClass(BaseRequest.class);
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
