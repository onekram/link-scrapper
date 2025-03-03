package backend.academy.bot.state;

import backend.academy.bot.service.ChatService;
import backend.academy.bot.service.LinksService;
import backend.academy.bot.state.filter.MessageTextFilter;
import backend.academy.bot.state.filter.StateFilter;
import backend.academy.bot.state.handler.Handler;
import backend.academy.bot.state.handler.MessageHandler;
import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Collections;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlerConfiguration {
    @Bean
    public Handler startHandler(ChatService chatService) {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.START))
            .withFilter(new MessageTextFilter("/start"))
            .nextState(State.MENU)
            .method(handlerContext -> {
                Long id = handlerContext.message().chat().id();
                chatService.registerChat(id);
                return new SendMessage(
                    id,
                    "Hello! Send \uD83D\uDDE3 /help to see list of command!" // TODO Create message_ru.properties
                );
            })
            .keyboard(new ReplyKeyboardRemove())
            .build();
    }

    @Bean
    public Handler startFromAnyStateHandler(ChatService chatService) {
        return MessageHandler.builder()
            .withFilter(new MessageTextFilter("/start"))
            .nextState(State.MENU)
            .method(handlerContext -> {
                Long id = handlerContext.message().chat().id();
                chatService.registerChat(id);
                return new SendMessage(
                    id,
                    "You jump to start! Send \uD83D\uDDE3 /help to see list of command!"
                );
            })
            .keyboard(new ReplyKeyboardRemove())
            .build();
    }

    @Bean
    public Handler helpHandler() {
        return MessageHandler.builder()
            .withFilter(new MessageTextFilter("/help"))
            .message("Available list of commands:\n▶️ /start\n\uD83D\uDDE3 /help\n\uD83D\uDD0D /track\n\uD83D\uDEAB /untrack\n\uD83D\uDCC3 /list")
            .keyboard(new ReplyKeyboardRemove())
            .build();
    }

    @Bean
    public Handler listHandler(LinksService linksService) {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.MENU))
            .withFilter(new MessageTextFilter("/list"))
            .nextState(State.MENU)
            .method(handlerContext -> {
                Long id = handlerContext.message().chat().id();
                ListLinksResponse response = linksService.getTrackedLinks(id);
                String links = response.getLinks().stream() // TODO Create message util class or smth
                    .map(LinkResponse::getUrl).collect(Collectors.joining("\n"));
                return new SendMessage(
                    id,
                    links.isEmpty()
                        ? "\uD83E\uDD14 It seems like you don't track any links\nUse \uD83D\uDD0D /track command"
                        : links
                );
            })
            .keyboard(new ReplyKeyboardRemove())
            .build();
    }

    @Bean
    public Handler trackHandler() {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.MENU))
            .withFilter(new MessageTextFilter("/track"))
            .nextState(State.TRACK_LINK)
            .message("\uD83D\uDCDD Input link to resource...")
            .keyboard(new ReplyKeyboardRemove())
            .build();
    }

    @Bean
    public Handler linkHandler(LinksService linksService) {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.TRACK_LINK))
            .nextState(State.TRACK_TAGS)
            .method((handlerContext -> {
                Long id = handlerContext.message().chat().id();
                linksService.trackLink(
                    id,
                    new AddLinkRequest(handlerContext.message().text(), Collections.emptyList(), Collections.emptyList())
                    //TODO Create builder for AddLinkRequest and put in in handlerContext
                );
                return new SendMessage(
                    id,
                    "\uD83C\uDFF7 Input tags..."
                );
            }))
            .keyboard(
                new ReplyKeyboardMarkup("Work", "Study")
                    .oneTimeKeyboard(true)
                    .resizeKeyboard(true))
            .build();
    }

    @Bean
    public Handler tagsHandler() {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.TRACK_TAGS))
            .nextState(State.MENU)
            .message("✅ Saved your choice")
            .keyboard(new ReplyKeyboardRemove())
            .build();
        //TODO Build AddLinkRequest with tags
    }

    //TODO FilterHandler

    @Bean
    Handler unTrack(LinksService linksService) {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.MENU))
            .withFilter(new MessageTextFilter("/untrack"))
            .nextState(State.UNTRACK_LINK)
            .method(handlerContext -> {
                Long id = handlerContext.message().chat().id();
                String[] links = linksService.getTrackedLinks(id).getLinks().stream()
                    .map(LinkResponse::getUrl)
                    .toArray(String[]::new);
                return new SendMessage(
                    id,
                    "✅ Choose links to untrack \uD83D\uDC47"
                ).replyMarkup(new ReplyKeyboardMarkup(links));
            })
            .build();
    }

    @Bean
    Handler inputLinkToUnTrack(LinksService linksService) {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.UNTRACK_LINK))
            .nextState(State.MENU)
            .method(handlerContext -> {
                Message message = handlerContext.message();
                Long chatId = message.chat().id();
                LinkResponse linkResponse = linksService.untrackLink(chatId, new RemoveLinkRequest(message.text()));
                return new SendMessage(
                    chatId,
                    "\uD83D\uDEAB Unsubscribed from " + linkResponse.getUrl()
                );
            })
            .keyboard(new ReplyKeyboardRemove())
            .build();
    }

    @Bean
    Handler unrecognizedAnswerHandler() {
        return MessageHandler.builder()
            .message("\uD83E\uDD37 I don't no such command, try \uD83D\uDD0D /help to find necessary one...")
            .build();
    }
}
