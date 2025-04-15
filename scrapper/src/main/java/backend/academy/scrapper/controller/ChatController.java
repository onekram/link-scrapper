package backend.academy.scrapper.controller;

import backend.academy.model.ApiErrorResponse;
import backend.academy.scrapper.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tg-chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @Operation(summary = "Зарегистрировать чат")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Чат зарегистрирован"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class)))
            })
    @PostMapping(
            value = "/{id}",
            produces = {"application/json"})
    public void registerChat(@PathVariable Long id) {
        chatService.register(id);
    }

    @Operation(summary = "Удалить чат")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Чат успешно удалён"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class))),
                @ApiResponse(
                        responseCode = "404",
                        description = "Чат не существует",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class)))
            })
    @DeleteMapping(
            value = "/{id}",
            produces = {"application/json"})
    public void unRegisterChat(@PathVariable Long id) {
        chatService.unRegister(id);
    }
}
