package backend.academy.scrapper.client.model;

public record GithubApiError(String message, String documentationURL, String status) {}
