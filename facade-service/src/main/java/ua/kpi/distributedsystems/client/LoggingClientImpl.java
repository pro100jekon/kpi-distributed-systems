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

@Component
public class LoggingClientImpl implements LoggingClient {

    private static final Logger log = LoggerFactory.getLogger(LoggingClientImpl.class);

    private final WebClient webClient;

    public LoggingClientImpl(@Value("${http-client.logging-service-url}") String loggingServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(loggingServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Override
    public Mono<Void> writeLog(LogMessageRequest request) {
        return webClient.post()
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(2))
                        .doBeforeRetry(retrySignal -> log.warn("Retrying log message... attempt {}", retrySignal.totalRetries() + 1))
                        .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) ->
                                new RuntimeException("Failed to send a log after retries", retrySignal.failure()))))
                .then();
    }

    @Override
    public Mono<LogResponse> getLog() {
        return webClient.get()
                .retrieve()
                .bodyToMono(LogResponse.class);
    }
}
