package ua.kpi.distributedsystems.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ua.kpi.distributedsystems.model.downstream.LogMessageRequest;
import ua.kpi.distributedsystems.model.downstream.LogResponse;

import java.time.Duration;

public class LoggingClientImpl implements LoggingClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingClientImpl.class);
    private static final String PATH = "/logging";

    private final WebClient webClient;

    public LoggingClientImpl(DiscoveryClient discoveryClient) {
        this.webClient = WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .filter(new RoundRobinExchangeFilter(discoveryClient, "logging-service"))
                .build();
    }

    @Override
    public Mono<Void> writeLog(LogMessageRequest request) {
        return webClient.post()
                .uri(PATH)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(2))
                        .doBeforeRetry(retrySignal -> log.warn("Retrying request... attempt {}", retrySignal.totalRetries() + 1))
                        .onRetryExhaustedThrow(((_, retrySignal) ->
                                new RuntimeException("Failed to send a request after retries", retrySignal.failure()))))
                .then();
    }

    @Override
    public Mono<LogResponse> getLog() {
        return webClient.get()
                .uri(PATH)
                .retrieve()
                .bodyToMono(LogResponse.class);
    }
}
