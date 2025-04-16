package ua.kpi.distributedsystems.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ua.kpi.distributedsystems.model.downstream.LogMessageRequest;
import ua.kpi.distributedsystems.model.downstream.LogResponse;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class LoggingClientImpl implements LoggingClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingClientImpl.class);

    private final WebClient webClient;
    private final List<String> hosts;
    private final String prefix;
    private final String suffix;

    public LoggingClientImpl(@Value("${http-client.logging-service-url}") String loggingServiceUrl) {
        this.webClient = WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .filter((req, next) -> {
                    log.info("Sending a request to {}:{}", req.url().getHost(), req.url().getPort());
                    return next.exchange(req);
                })
                .build();
        var urlParts = Pattern.compile("(?<=//).+(?=/)").splitWithDelimiters(loggingServiceUrl, 0);
        this.hosts = Arrays.asList(urlParts[1].split(","));
        this.prefix = urlParts[0];
        this.suffix = Arrays.stream(urlParts).skip(2).collect(Collectors.joining());
    }

    @Override
    public Mono<Void> writeLog(LogMessageRequest request) {
        return getUriWithRandomHost()
                .flatMap(uri ->
                        webClient.post()
                                .uri(uri)
                                .bodyValue(request)
                                .retrieve()
                                .toBodilessEntity()
                                .retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(2))
                                        .doBeforeRetry(retrySignal -> log.warn("Retrying log message... attempt {}", retrySignal.totalRetries() + 1))
                                        .onRetryExhaustedThrow(((_, retrySignal) ->
                                                new RuntimeException("Failed to send a log after retries", retrySignal.failure()))))
                                .then()
                );
    }

    @Override
    public Mono<LogResponse> getLog() {
        return getUriWithRandomHost()
                .flatMap(uri -> webClient.get()
                        .uri(uri)
                        .retrieve()
                        .bodyToMono(LogResponse.class));
    }

    private Mono<String> getUriWithRandomHost() {
        return Mono.fromSupplier(() -> {
            var host = hosts.get(ThreadLocalRandom.current().nextInt(hosts.size()));
            return prefix + host + suffix;
        });
    }
}
