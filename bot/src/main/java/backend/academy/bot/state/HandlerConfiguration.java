package backend.academy.bot.state;

import backend.academy.bot.repository.parameters.ContextRepository;
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
import com.pengrad.telegrambot.model.LinkPreviewOptions;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class HandlerConfiguration {
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
                    return new SendMessage(id, resourceBundle.getString("welcome.message"));
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
                    return new SendMessage(id, resourceBundle.getString("start.message"));
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
                    String links = response.links().stream()
                            .map(MessageUtil::linkMessage)
                            .collect(Collectors.joining("\n"));
                    return new SendMessage(id, links.isEmpty() ? resourceBundle.getString("no.links.message") : links)
                            .linkPreviewOptions(new LinkPreviewOptions().isDisabled(true));
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
    public Handler linkHandler(ContextRepository contextRepository) {
        return MessageHandler.builder()
                .withFilter(new StateFilter(State.TRACK_LINK))
                .nextState(State.TRACK_TAGS)
                .method(handlerContext -> {
                    Long chatId = handlerContext.message().chat().id();
                    String textLink = handlerContext.message().text().strip();
                    AddLinkRequest.Builder builder = AddLinkRequest.builder().link(textLink);
                    contextRepository.setContext(chatId, builder);
                    return new SendMessage(chatId, resourceBundle.getString("input.tags.message"));
                })
                .keyboard(new ReplyKeyboardMarkup("Work", "Study")
                        .oneTimeKeyboard(true)
                        .resizeKeyboard(true))
                .menuButton(true)
                .build();
    }

    @Bean
    public Handler tagsHandler(ContextRepository contextRepository) {
        return MessageHandler.builder()
                .withFilter(new StateFilter(State.TRACK_TAGS))
                .nextState(State.TRACK_FILTERS)
                .method(handlerContext -> {
                    Long chatId = handlerContext.message().chat().id();
                    String text = handlerContext.message().text().strip();
                    AddLinkRequest.Builder builder = contextRepository
                            .getContext(chatId, AddLinkRequest.Builder.class)
                            .orElseThrow(() ->
                                    new RuntimeException("No AddLinkRequest building exist for chatId: " + chatId));
                    builder.tags(List.of(text.split("\\s+")));
                    contextRepository.setContext(chatId, builder);
                    return new SendMessage(chatId, resourceBundle.getString("input.filters.message"));
                })
                .menuButton(true)
                .keyboard(new ReplyKeyboardRemove())
                .build();
    }

    @Bean
    public Handler filterHandler(LinksService linksService, ContextRepository contextRepository) {
        return MessageHandler.builder()
                .withFilter(new StateFilter(State.TRACK_FILTERS))
                .nextState(State.MENU)
                .method(handlerContext -> {
                    Long chatId = handlerContext.message().chat().id();
                    String text = handlerContext.message().text().strip();
                    AddLinkRequest.Builder builder = contextRepository
                            .getContext(chatId, AddLinkRequest.Builder.class)
                            .orElseThrow(() ->
                                    new RuntimeException("No AddLinkRequest building exist for chatId: " + chatId));
                    builder.filters(List.of(text.split("\\s+")));
                    contextRepository.deleteContext(chatId, AddLinkRequest.Builder.class);
                    AddLinkRequest addLinkRequest = builder.build();
                    contextRepository.setContext(chatId, addLinkRequest);
                    linksService.trackLink(chatId, addLinkRequest);
                    return new SendMessage(chatId, resourceBundle.getString("saved.message"));
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
                    List<LinkResponse> linkResponses =
                            linksService.getTrackedLinks(id).links();
                    String[][] links = linkResponses.stream()
                            .map(LinkResponse::url)
                            .map(s -> new String[] {s})
                            .toArray(String[][]::new);
                    String[] tags = linkResponses.stream()
                            .map(LinkResponse::tags)
                            .flatMap(List::stream)
                            .distinct()
                            .map(resourceBundle.getString("all.in.tag.message")::formatted)
                            .toArray(String[]::new);
                    return new SendMessage(id, resourceBundle.getString("untrack.links.message"))
                            .replyMarkup(new ReplyKeyboardMarkup(links)
                                    .addRow(tags)
                                    .addRow(resourceBundle.getString("menu.message"))
                                    .resizeKeyboard(true));
                })
                .build();
    }

    @Bean
    public Handler inputTagToUnTrack(LinksService linksService) {
        return MessageHandler.builder()
                .withFilter(new StateFilter(State.UNTRACK_LINK))
                .withFilter(context -> context.message()
                        .text()
                        .trim()
                        .startsWith(StringUtils.left(resourceBundle.getString("all.in.tag.message"), 10)))
                .nextState(State.MENU)
                .method(handlerContext -> {
                    Message message = handlerContext.message();
                    Pattern pattern = Pattern.compile("All in tag (\\w+)");
                    Matcher matcher = pattern.matcher(message.text().trim());
                    if (!matcher.find()) {
                        throw new RuntimeException("Message should match tag");
                    }
                    String tag = matcher.group(1);
                    Long chatId = message.chat().id();
                    String answer = linksService.getTrackedLinks(chatId).links().stream()
                            .filter(linkResponse -> linkResponse.tags().contains(tag))
                            .map(linkResponse ->
                                    linksService.untrackLink(chatId, new RemoveLinkRequest(linkResponse.url())))
                            .map(linkResponse ->
                                    resourceBundle.getString("unsubscribed.message") + " " + linkResponse.url())
                            .collect(Collectors.joining("\n"));
                    return new SendMessage(chatId, answer);
                })
                .keyboard(new ReplyKeyboardRemove())
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
                            chatId, resourceBundle.getString("unsubscribed.message") + " " + linkResponse.url());
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
