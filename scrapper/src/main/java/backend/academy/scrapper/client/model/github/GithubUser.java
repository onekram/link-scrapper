package backend.academy.scrapper.client.model.github;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GithubUser(String login, @JsonProperty("html_url") String url) {}
