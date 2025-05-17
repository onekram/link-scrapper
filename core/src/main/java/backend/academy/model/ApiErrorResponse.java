package backend.academy.model;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;

public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {
    public static ApiErrorResponse fromException(Exception exception, String description, HttpStatus status) {
        return new ApiErrorResponse(
                description,
                String.valueOf(status.value()),
                exception.getClass().getSimpleName(),
                Objects.requireNonNullElse(exception.getMessage(), ""),
                Arrays.stream(exception.getStackTrace())
                        .map(StackTraceElement::toString)
                        .toList());
    }
}
