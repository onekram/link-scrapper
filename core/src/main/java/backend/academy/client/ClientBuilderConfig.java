package backend.academy.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class ClientBuilderConfig {

    @Bean
    public ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.info("Request: {} {}", request.method(), request.url());
            request.headers()
                    .forEach(
                            (name, values) -> values.forEach(value -> log.debug("Request header: {}={}", name, value)));
            return Mono.just(request);
        });
    }

    @Bean
    public ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            log.info("Response status: {}", response.statusCode());
            response.headers()
                    .asHttpHeaders()
                    .forEach((name, values) ->
                            values.forEach(value -> log.debug("Response header: {}={}", name, value)));
            return Mono.just(response);
        });
    }
}
