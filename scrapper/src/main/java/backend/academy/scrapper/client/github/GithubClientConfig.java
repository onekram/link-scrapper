package backend.academy.scrapper.client.github;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.client.model.GithubApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class GithubClientConfig {

    @Bean
    public WebClient githubWebClient(
            ScrapperConfig scrapperConfig, ExchangeFilterFunction logRequest, ExchangeFilterFunction logResponse) {
        return WebClient.builder()
                .baseUrl(scrapperConfig.githubApiUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + scrapperConfig.githubToken())
                .filter(logRequest)
                .filter(logResponse)
                .filter(errorHandler())
                .build();
    }

    private ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().isError()) {
                return response.bodyToMono(GithubApiError.class)
                        .flatMap(errorBody -> {
                            log.error("API Error: {} | Message: {}", errorBody.status(), errorBody.message());
                            return Mono.error(new RuntimeException());
                        })
                        .thenReturn(response);
            }
            return Mono.just(response);
        });
    }

    @Bean
    public GithubReposClient githubClient(WebClient githubWebClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(githubWebClient))
                .build();
        return factory.createClient(GithubReposClient.class);
    }
}
