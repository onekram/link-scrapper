package backend.academy.bot.client;

import backend.academy.model.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Configuration
public class ClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .baseUrl("http://localhost:8081") // TODO Set independent url
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .filter((request, next) -> {
                log.info("Request: {} {}", request.method(), request.url());
                return next.exchange(request)
                    .doOnNext(response -> log.info("Response: {}", response.statusCode()))
                    .doOnError(WebClientResponseException.class, e -> {
                        ApiErrorResponse error = e.getResponseBodyAs(ApiErrorResponse.class);
                        if (error == null) {
                            log.error("API Error code: {}, can't retrieve original error", e.getStatusCode());
                        } else {
                            log.error("API Error: {} - {}", e.getStatusCode(), error.getExceptionMessage());
                        }
                    });
            })
            .build(); // TODO Add retries
    }

    @Bean
    public LinksClient linksClient(WebClient webClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClient))
            .build();

        return factory.createClient(LinksClient.class);
    }

    @Bean
    public ChatClient chatClient(WebClient webClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(webClient))
            .build();

        return factory.createClient(ChatClient.class);
    }
}
