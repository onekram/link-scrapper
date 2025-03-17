package backend.academy.bot.client;

import backend.academy.model.ApiErrorResponse;
import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ApiErrorResponse errorResponse;

    public ApiException(ApiErrorResponse errorResponse) {
        super(errorResponse.getDescription());
        this.errorResponse = errorResponse;
    }
}
