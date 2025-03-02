package backend.academy.bot.state;

import backend.academy.bot.repository.user.UserRecord;
import backend.academy.bot.repository.user.UserRepository;
import backend.academy.bot.state.filter.MessageTextFilter;
import backend.academy.bot.state.filter.PresentUserFilter;
import backend.academy.bot.state.filter.StateFilter;
import backend.academy.bot.state.handler.MessageHandler;
import backend.academy.bot.state.handler.Handler;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;

@Configuration
public class HandlerConfiguration {
    @Bean
    public Handler startHandler() {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.START))
            .withFilter(new MessageTextFilter("/start"))
            .nextState(State.LOGIN)
            .message("Hello, tell me who are you?")
            .build();
    }

    @Bean
    public Handler startFromAnyStateHandler() {
        return MessageHandler.builder()
            .withFilter(new MessageTextFilter("/start"))
            .nextState(State.LOGIN)
            .message("Hello, you jump to start, tell me who are you?")
            .build();
    }

    @Bean
    public Handler helpHandler() {
        return MessageHandler.builder()
            .withFilter(new MessageTextFilter("/help"))
            .message("Available list of commands: /start /help /track /untrack /list")
            .build();
    }

    @Bean
    public Handler loginHandler(UserRepository userRepository) {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.LOGIN))
            .withFilter(new PresentUserFilter(userRepository))
            .nextState(State.MENU)
            .message("Long time no see! Send /help to see list of command!")
            .build();
    }

    @Bean
    public Handler loginDefaultHandler(UserRepository userRepository) {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.LOGIN))
            .nextState(State.MENU)
            .method(handlerContext -> {
                String login = handlerContext.message().text();
                userRepository.saveUser(
                    login,
                    new UserRecord(new ArrayList<>()));
                return new SendMessage(
                    handlerContext.message().chat().id(),
                    String.format("Welcome to the club %s. Send /help to see list of commands!", login)
                );
            })
            .build();
    }

    @Bean
    public Handler listHandler() {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.MENU))
            .withFilter(new MessageTextFilter("/list"))
            .nextState(State.MENU)
            .message("Here would be list of your tracking resources")
            .build();
    }

    @Bean
    public Handler trackHandler() {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.MENU))
            .withFilter(new MessageTextFilter("/track"))
            .nextState(State.TRACK_LINK)
            .message("Input link to resource")
            .build();
    }

    @Bean
    public Handler linkHandler() {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.TRACK_LINK))
            .nextState(State.TRACK_TAGS)
            .message("Input tags or choose form panel")
            .keyboard(
                new ReplyKeyboardMarkup("Work", "Study")
                    .oneTimeKeyboard(true)
                    .resizeKeyboard(true)
                    .selective(true))
            .build();
    }

    @Bean
    public Handler tagsHandler() {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.TRACK_TAGS))
            .nextState(State.MENU)
            .message("Saved your choice")
            .build();
    }

    @Bean Handler unTrack() {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.MENU))
            .withFilter(new MessageTextFilter("/untrack"))
            .nextState(State.MENU)
            .message("Here would be untracking logic")
            .build();
    }

    @Bean Handler unrecognizedAnswerHandler() {
        return MessageHandler.builder()
            .message("I don't no such command, try /help to find necessary one")
            .build();
    }
}
