package backend.academy.bot.controller;

import backend.academy.bot.service.UpdateService;
import backend.academy.model.ApiErrorResponse;
import backend.academy.model.LinkUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestController
@Slf4j
@RequiredArgsConstructor
public class UpdatesController {
    private final UpdateService updateService;

    @Operation(summary = "Отправить обновление")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "Обновление обработано"),
                @ApiResponse(
                        responseCode = "400",
                        description = "Некорректные параметры запроса",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ApiErrorResponse.class)))
            })
    @PostMapping(
            value = "/updates",
            produces = {"application/json"})
    public void sendUpdates(@RequestBody LinkUpdate linkUpdate) {
        updateService.updateProcess(linkUpdate);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorResponse> invalidParameters(Exception ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.fromException(ex, "Некорректные параметры запроса", status));
    }
}
