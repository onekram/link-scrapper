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
import java.util.ResourceBundle;
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
    private final ResourceBundle resourceBundle;

    @Bean
    public Handler menuHandler() {
        return MessageHandler.builder()
            .withFilter(new MessageTextFilter(resourceBundle.getString("menu.message")))
            .nextState(State.MENU)
            .message(resourceBundle.getString("available.list.of.commands.message"))
            .keyboard(new ReplyKeyboardRemove())
            .build();
    }

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
                    resourceBundle.getString("welcome.message")
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
                    resourceBundle.getString("start.message")
                );
            })
            .keyboard(new ReplyKeyboardRemove())
            .build();
    }

    @Bean
    public Handler helpHandler() {
        return MessageHandler.builder()
            .withFilter(new MessageTextFilter("/help"))
            .message(resourceBundle.getString("available.list.of.commands.message"))
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
                        ? resourceBundle.getString("no.links.message")
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
            .message(resourceBundle.getString("input.resource.link.message"))
            .keyboard(new ReplyKeyboardRemove())
            .menuButton(true)
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
                    resourceBundle.getString("input.tags.message")
                );
            }))
            .keyboard(
                new ReplyKeyboardMarkup("Work", "Study")
                    .oneTimeKeyboard(true)
                    .resizeKeyboard(true))
            .menuButton(true)
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
                    resourceBundle.getString("input.filters.message")
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
                    resourceBundle.getString("saved.message")
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
                    resourceBundle.getString("untrack.links.message")
                ).replyMarkup(new ReplyKeyboardMarkup(links).addRow(resourceBundle.getString("menu.message")));
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
                    resourceBundle.getString("unsubscribed.message") + " " + linkResponse.getUrl()
                );
            })
            .keyboard(new ReplyKeyboardRemove())
            .build();
    }

    @Bean
    public Handler unrecognizedAnswerHandler() {
        return MessageHandler.builder()
            .message(resourceBundle.getString("unsupported.command.message"))
            .menuButton(true)
            .build();
    }
}
