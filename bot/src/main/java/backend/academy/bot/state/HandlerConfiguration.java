package backend.academy.bot.state;

import backend.academy.bot.service.ChatService;
import backend.academy.bot.service.LinksService;
import backend.academy.bot.state.filter.MessageTextFilter;
import backend.academy.bot.state.filter.StateFilter;
import backend.academy.bot.state.handler.Handler;
import backend.academy.bot.state.handler.MessageHandler;
import backend.academy.bot.util.MessageUtil;
import backend.academy.model.AddLinkRequest;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HandlerConfiguration {
    public static final @NotNull String ADD_LINK_BUILDER = "addLinkBuilder";

    private final HandlerContextParameters handlerContextParameters;

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
                String links = response.getLinks().stream()
                    .map(MessageUtil::linkMessage)
                    .collect(Collectors.joining("\n"));
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
    public Handler linkHandler() {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.TRACK_LINK))
            .nextState(State.TRACK_TAGS)
            .method((handlerContext -> {
                Long id = handlerContext.message().chat().id();
                String textLink = handlerContext.message().text().strip();
                AddLinkRequest.Builder builder = new AddLinkRequest.Builder().link(textLink);
                handlerContextParameters.setParameter(ADD_LINK_BUILDER, builder);
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
            .nextState(State.TRACK_FILTERS)
            .method(handlerContext -> {
                Long id = handlerContext.message().chat().id();
                String text = handlerContext.message().text().strip();
                AddLinkRequest.Builder builder = handlerContextParameters
                    .getParameter(ADD_LINK_BUILDER, AddLinkRequest.Builder.class);
                builder.tags(List.of(text.split("\\s+")));
                return new SendMessage(
                    id,
                    "\uD83D\uDD0D Input filters..."
                );
            })
            .keyboard(new ReplyKeyboardRemove())
            .build();
    }

    @Bean
    public Handler filterHandler(LinksService linksService) {
        return MessageHandler.builder()
            .withFilter(new StateFilter(State.TRACK_FILTERS))
            .nextState(State.MENU)
            .method(handlerContext -> {
                Long id = handlerContext.message().chat().id();
                String text = handlerContext.message().text().strip();
                AddLinkRequest.Builder builder = handlerContextParameters
                    .getParameter(ADD_LINK_BUILDER, AddLinkRequest.Builder.class);
                builder.filters(List.of(text.split("\\s+")));
                handlerContextParameters.clearParameter(ADD_LINK_BUILDER);
                linksService.trackLink(id, builder.build());
                return new SendMessage(
                    id,
                    "✅ Saved your choice"
                );
            })
            .keyboard(new ReplyKeyboardRemove())
            .build();
    }

    @Bean
    public Handler unTrack(LinksService linksService) {
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
    public Handler inputLinkToUnTrack(LinksService linksService) {
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
    public Handler unrecognizedAnswerHandler() {
        return MessageHandler.builder()
            .message("\uD83E\uDD37 I don't no such command, try \uD83D\uDD0D /help to find necessary one...")
            .build();
    }
}
