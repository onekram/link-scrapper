package backend.academy.scrapper.controller;

import backend.academy.model.ApiErrorResponse;
import backend.academy.scrapper.exception.BadRequestException;
import backend.academy.scrapper.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ScrapperControllerExceptionHandler {

    @ExceptionHandler({
        BadRequestException.class,
        MethodArgumentTypeMismatchException.class,
        HttpMessageNotReadableException.class,
        MissingRequestHeaderException.class
    })
    public ResponseEntity<ApiErrorResponse> convertPojoExceptionHandler(Exception ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.fromException(ex, "Некорректные параметры запроса", status));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.fromException(ex, "Запрашиваемый ресурс не найден", status));
    }
}
