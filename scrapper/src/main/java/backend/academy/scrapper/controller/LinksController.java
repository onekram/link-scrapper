package backend.academy.scrapper.controller;

import backend.academy.model.AddLinkRequest;
import backend.academy.model.ApiErrorResponse;
import backend.academy.model.LinkResponse;
import backend.academy.model.ListLinksResponse;
import backend.academy.model.RemoveLinkRequest;
import backend.academy.scrapper.service.LinksService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
public class LinksController {
    private final LinksService linksService;

    @Operation(summary = "Получить все отслеживаемые ссылки")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Ссылки успешно получены",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ListLinksResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class)))
            })
    @GetMapping(
            produces = {"application/json"},
            headers = {"Tg-Chat-Id"})
    public ListLinksResponse getLinks(@RequestHeader("Tg-Chat-Id") Long tgChatId) {
        return linksService.listAll(tgChatId);
    }

    @Operation(summary = "Добавить отслеживание ссылки")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Ссылка успешно добавлена",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = LinkResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class)))
            })
    @PostMapping(
            produces = {"application/json"},
            consumes = {"application/json"},
            headers = {"Tg-Chat-Id"})
    public LinkResponse addLink(@RequestHeader("Tg-Chat-Id") Long tgChatId, @RequestBody AddLinkRequest request) {
        return linksService.addLink(tgChatId, request);
    }

    @Operation(summary = "Убрать отслеживание ссылки")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Ссылка успешно убрана",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = LinkResponse.class))),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Ссылка не найдена",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class)))
            })
    @DeleteMapping(
            produces = {"application/json"},
            consumes = {"application/json"},
            headers = {"Tg-Chat-Id"})
    public LinkResponse removeLink(@RequestHeader("Tg-Chat-Id") Long tgChatId, @RequestBody RemoveLinkRequest request) {
        return linksService.removeLink(tgChatId, request);
    }
}
