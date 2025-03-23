package backend.academy.bot.controller;

import backend.academy.model.ApiErrorResponse;
import backend.academy.model.LinkUpdate;
import com.pengrad.telegrambot.Callback;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UpdatesController {
    private final TelegramBot telegramBot;

    @Operation(summary = "Отправить обновление")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Обновление обработано"),
        @ApiResponse(
            responseCode = "400",
            description = "Некорректные параметры запроса",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiErrorResponse.class))) })
    @PostMapping(
        value ="/updates",
        produces = { "application/json" })
    public ResponseEntity<Void> sendUpdates(@RequestBody LinkUpdate linkUpdate) {
        linkUpdate.getTgChatIds().forEach(
            chatId -> telegramBot.execute(new SendMessage(chatId, "Updates detected"), new Callback<SendMessage, SendResponse>() {
                @Override
                public void onResponse(SendMessage request, SendResponse response) {
                    log.info("Message sent: {} with response: {}", request, response);
                }

                @Override
                public void onFailure(SendMessage request, IOException e) {
                    log.info("Message sent: {} with error", request, e);
                }
            }));
        return ResponseEntity.ok().build();
    }
}
