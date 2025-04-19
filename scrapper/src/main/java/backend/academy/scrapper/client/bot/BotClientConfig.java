package backend.academy.scrapper.client.bot;

import backend.academy.model.ApiErrorResponse;
import backend.academy.scrapper.ScrapperConfig;
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

@Configuration
@Slf4j
public class BotClientConfig {

    @Bean
    public WebClient botWebClient(
            ScrapperConfig scrapperConfig, ExchangeFilterFunction logRequest, ExchangeFilterFunction logResponse) {
        return WebClient.builder()
                .baseUrl(scrapperConfig.botUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .filter(logRequest)
                .filter(logResponse)
                .filter(errorHandler())
                .build();
    }

    private ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().isError()) {
                return response.bodyToMono(ApiErrorResponse.class)
                        .flatMap(errorBody -> {
                            log.error(
                                    "API Error: {} - {} | Exception name: {} | Exception message: {}",
                                    errorBody.code(),
                                    errorBody.description(),
                                    errorBody.exceptionName(),
                                    errorBody.exceptionMessage());
                            return Mono.error(new RuntimeException());
                        })
                        .thenReturn(response);
            }
            return Mono.just(response);
        });
    }

    @Bean
    public UpdatesClient botClient(WebClient botWebClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(botWebClient))
                .build();
        return factory.createClient(UpdatesClient.class);
    }
}
