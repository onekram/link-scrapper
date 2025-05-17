package backend.academy.scrapper.client.model.github;

public record GithubApiError(String message, String documentationURL, String status) {}
