package backend.academy.bot.state.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.bot.state.HandlerContext;
import backend.academy.bot.state.State;
import backend.academy.bot.util.TestUtil;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.ResourceBundle;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MessageHandlerTest {

    @Mock
    private ResourceBundle resourceBundle;

    @Mock
    private HandlerContext context;

    @Mock
    private TelegramBot bot;

    @Test
    @DisplayName("HappyPath filters passed")
    void filtersPasssed() {
        when(resourceBundle.getString("menu.message")).thenReturn("Menu");
        when(context.message()).thenReturn(TestUtil.generateMessage("mytext", 123L));
        when(context.bot()).thenReturn(bot);

        MessageHandler handler = MessageHandler.builder()
                .withFilter(ignore -> true)
                .message("Test message")
                .menuButton(true)
                .build();
        setResourceBundle(handler, resourceBundle);
        invokePostConstruct(handler);

        boolean result = handler.handle(context);

        assertTrue(result);
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(bot).execute(captor.capture());
        SendMessage sent = captor.getValue();
        assertEquals(123L, TestUtil.getId(sent));
        assertEquals("Test message", TestUtil.getText(sent));
    }

    @Test
    @DisplayName("HappyPath filters not passed")
    void filtersNotPasssed() {
        when(resourceBundle.getString("menu.message")).thenReturn("Menu");

        MessageHandler handler = MessageHandler.builder()
                .withFilter(ignore -> false)
                .message("Test message")
                .menuButton(true)
                .build();
        setResourceBundle(handler, resourceBundle);
        invokePostConstruct(handler);

        boolean result = handler.handle(context);

        assertFalse(result);
        verifyNoInteractions(context);
        verifyNoInteractions(bot);
    }

    @Test
    @DisplayName("Custom method")
    void customMethod() {
        SendMessage sendMessage = spy(new SendMessage(123L, "Custom"));
        Function<HandlerContext, SendMessage> customMethod = ignore -> sendMessage;
        when(context.bot()).thenReturn(bot);

        MessageHandler handler = MessageHandler.builder()
                .withFilter(ignore -> true)
                .method(customMethod)
                .build();

        boolean result = handler.handle(context);

        assertTrue(result);
        verify(sendMessage, never()).replyMarkup(any());
        verify(bot).execute(argThat(req -> "Custom".equals(TestUtil.getText(req))));
    }

    @Test
    @DisplayName("Keyboard field overrides method keyboard")
    void customMethodKeyboardOverrides() {
        SendMessage sendMessage =
                spy(new SendMessage(123L, "Custom").replyMarkup(new ReplyKeyboardMarkup("ButtonMethod")));
        Function<HandlerContext, SendMessage> customMethod = ignore -> sendMessage;
        when(context.bot()).thenReturn(bot);

        MessageHandler handler = MessageHandler.builder()
                .withFilter(ignore -> true)
                .method(customMethod)
                .keyboard(new ReplyKeyboardMarkup("ButtonField"))
                .build();

        boolean result = handler.handle(context);

        assertTrue(result);
        ArgumentCaptor<Keyboard> captor = ArgumentCaptor.forClass(Keyboard.class);

        verify(sendMessage, times(1)).replyMarkup(captor.capture());
        TestUtil.assertContainsButton(captor.getValue(), "ButtonField");
        TestUtil.assertDoesntContainButton(captor.getValue(), "ButtonMethod");
        verify(bot).execute(argThat(req -> "Custom".equals(TestUtil.getText(req))));
    }

    @Test
    @DisplayName("Custom method overrides message")
    void customMethodOverrides() {
        SendMessage sendMessage =
                spy(new SendMessage(123L, "Custom").replyMarkup(new ReplyKeyboardMarkup("ButtonMethod")));
        Function<HandlerContext, SendMessage> customMethod = ignore -> sendMessage;
        when(context.bot()).thenReturn(bot);

        MessageHandler handler = MessageHandler.builder()
                .withFilter(ignore -> true)
                .method(customMethod)
                .message("Not custom")
                .build();

        boolean result = handler.handle(context);

        assertTrue(result);
        verify(bot).execute(argThat(req -> "Custom".equals(TestUtil.getText(req))));
    }

    @Test
    @DisplayName("If keyboard bot present, set menu button")
    void setMenuIfKeyboardIsNull() {
        when(resourceBundle.getString("menu.message")).thenReturn("Menu");

        MessageHandler handler = MessageHandler.builder().menuButton(true).build();
        setResourceBundle(handler, resourceBundle);
        invokePostConstruct(handler);

        TestUtil.assertContainsButton(handler, "Menu");
    }

    @Test
    @DisplayName("Set menu button, when current is remove and menu enabled")
    void setMenuButtonIfRemoved() {
        when(resourceBundle.getString("menu.message")).thenReturn("Menu");

        MessageHandler handler = MessageHandler.builder()
                .keyboard(new ReplyKeyboardRemove())
                .menuButton(true)
                .build();
        setResourceBundle(handler, resourceBundle);
        invokePostConstruct(handler);

        TestUtil.assertContainsButton(handler, "Menu");
    }

    @Test
    @DisplayName("Add menu button to existing keyboard")
    void addMenuToExistinKeyboard() {
        when(resourceBundle.getString("menu.message")).thenReturn("Menu");

        ReplyKeyboardMarkup existing = new ReplyKeyboardMarkup("Button1");
        MessageHandler handler =
                MessageHandler.builder().keyboard(existing).menuButton(true).build();
        setResourceBundle(handler, resourceBundle);
        invokePostConstruct(handler);

        TestUtil.assertContainsButton(handler, "Menu", "Button1");
    }

    @Test
    @DisplayName("If menu button false keyboard is null")
    void keyboardIsNullWhenMenuNotEnabled() {
        MessageHandler handler = MessageHandler.builder().menuButton(false).build();
        invokePostConstruct(handler);
        assertNull(TestUtil.getKeyboardFromHandler(handler));
    }

    @Test
    @DisplayName("Default state is menu")
    void defaultState() {
        MessageHandler handler = MessageHandler.builder().build();
        invokePostConstruct(handler);
        assertEquals(State.MENU, handler.nextState());
    }

    private void invokePostConstruct(MessageHandler handler) {
        ReflectionTestUtils.invokeMethod(handler, "postConstruct");
    }

    private void setResourceBundle(MessageHandler handler, ResourceBundle resourceBundle) {
        ReflectionTestUtils.setField(handler, "resourceBundle", resourceBundle);
    }
}
