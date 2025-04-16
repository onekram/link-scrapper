package backend.academy.bot.service;

public class WebhookException extends RuntimeException {
    public WebhookException(String message) {
        super(message);
    }
}
