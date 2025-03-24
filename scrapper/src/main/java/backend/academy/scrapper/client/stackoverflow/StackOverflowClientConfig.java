package backend.academy.scrapper.client.stackoverflow;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.client.model.StackOverflowApiErrorResponse;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class StackOverflowClientConfig {

    @Bean
    public WebClient stackOverflowWebClient(ScrapperConfig scrapperConfig) {
        return WebClient.builder()
            .baseUrl(scrapperConfig.stackOverflowApiUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .filter(addQueryParams(
                scrapperConfig.stackOverflow().accessToken(),
                scrapperConfig.stackOverflow().key()
            ))
            .filter(logRequest())
            .filter(logResponse())
            .filter(errorHandler())
            .build();
    }

    private ExchangeFilterFunction addQueryParams(String accessToken, String key) {
        return (clientRequest, next) -> {
            URI modifiedUri = UriComponentsBuilder.fromUri(clientRequest.url())
                .queryParam("key", key)
                .queryParam("access_token", accessToken)
                .build()
                .toUri();

            ClientRequest filteredRequest = ClientRequest.from(clientRequest)
                .url(modifiedUri)
                .build();

            return next.exchange(filteredRequest);
        };
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.info("Request: {} {}", request.method(), request.url());
            request.headers().forEach((name, values) ->
                values.forEach(value -> log.debug("Request header: {}={}", name, value)));
            return Mono.just(request);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            log.info("Response status: {}", response.statusCode());
            response.headers().asHttpHeaders().forEach((name, values) ->
                values.forEach(value -> log.debug("Response header: {}={}", name, value)));
            return Mono.just(response);
        });
    }

    private ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().isError()) {
                return response.bodyToMono(StackOverflowApiErrorResponse.class)
                    .flatMap(errorBody -> {
                        log.error("API Error: {} | Error name: {} | Message: {}",
                            errorBody.getErrorId(),
                            errorBody.getErrorName(),
                            errorBody.getErrorMessage());
                        return Mono.error(new RuntimeException());
                    })
                    .thenReturn(response);
            }
            return Mono.just(response);
        });
    }

    @Bean
    public StackOverflowQuestionClient stackOverflowClient(WebClient stackOverflowWebClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(stackOverflowWebClient))
            .build();
        return factory.createClient(StackOverflowQuestionClient.class);
    }
}
