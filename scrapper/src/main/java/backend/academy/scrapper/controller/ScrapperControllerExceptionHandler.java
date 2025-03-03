package backend.academy.scrapper.controller;

import backend.academy.scrapper.exception.BadRequestException;
import backend.academy.model.ApiErrorResponse;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ScrapperControllerExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(ex.getStatus()).body(
            new ApiErrorResponse(
                "Некорректные параметры запроса",
                String.valueOf(ex.getStatus().value()),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                Stream.of(ex.getStackTrace()).map(StackTraceElement::toString).toList()
            )
        );
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorResponse> convertPojoExceptionHandler(Exception ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(
            new ApiErrorResponse(
                "Некорректные параметры запроса",
                String.valueOf(status.value()),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                Stream.of(ex.getStackTrace()).map(StackTraceElement::toString).toList()
            )
        );
    }
}
