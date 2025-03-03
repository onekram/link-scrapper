package backend.academy.scrapper.controller;

import backend.academy.scrapper.exception.BadRequestException;
import backend.academy.scrapper.exception.NotFoundException;
import backend.academy.scrapper.model.ApiErrorResponse;
import backend.academy.scrapper.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
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
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Чат зарегистрирован"),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры запроса",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))) })
    @PostMapping(
        value ="/{id}",
        produces = { "application/json" })
    public ResponseEntity<Void> registerChat(@PathVariable Long id) {
        if (id < 0) {
            throw new BadRequestException(String.format("Невалидный идентификатор чата: %s", id));
        }
        chatService.register(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить чат")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Чат успешно удалён"),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры запроса",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Чат не существует",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))) })
    @DeleteMapping(
        value = "/{id}",
        produces = { "application/json" })
    public ResponseEntity<Void> unRegisterChat(@PathVariable Long id) {
        if (id < 0) {
            throw new BadRequestException(String.format("Невалидный идентификатор чата: %s", id));
        }
        chatService.unRegister(id);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(ex.getStatus()).body(
            new ApiErrorResponse(
                "Чат не найден",
                String.valueOf(ex.getStatus().value()),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                Stream.of(ex.getStackTrace()).map(StackTraceElement::toString).toList()
            )
        );
    }
}
