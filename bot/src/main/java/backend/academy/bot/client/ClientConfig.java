package backend.academy.bot.client;

import backend.academy.bot.BotConfig;
import backend.academy.bot.exception.ApiException;
import backend.academy.model.ApiErrorResponse;
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
public class ClientConfig {

    @Bean
    public WebClient webClient(
            BotConfig botConfig, ExchangeFilterFunction logRequest, ExchangeFilterFunction logResponse) {
        return WebClient.builder()
                .baseUrl(botConfig.scrapperUrl())
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
                                    errorBody.getCode(),
                                    errorBody.getDescription(),
                                    errorBody.getExceptionName(),
                                    errorBody.getExceptionMessage());
                            return Mono.error(new ApiException(errorBody));
                        })
                        .thenReturn(response);
            }
            return Mono.just(response);
        });
    }

    @Bean
    public LinksClient linksClient(WebClient webClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(LinksClient.class);
    }

    @Bean
    public ChatClient chatClient(WebClient webClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient))
                .build();

        return factory.createClient(ChatClient.class);
    }
}
