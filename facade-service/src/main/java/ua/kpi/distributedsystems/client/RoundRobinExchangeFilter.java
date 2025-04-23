package ua.kpi.distributedsystems.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinExchangeFilter implements ExchangeFilterFunction {

    private static final Logger log = LoggerFactory.getLogger(RoundRobinExchangeFilter.class);

    private final AtomicInteger counter = new AtomicInteger(0);
    private final List<String> hosts;

    public RoundRobinExchangeFilter(List<String> hosts) {
        this.hosts = hosts;
    }

    @Override
    @NonNull
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        var baseUrl = getNextBaseUrl();
        var originalUri = request.url();
        var newUri = URI.create(baseUrl + originalUri.getPath()
                + (originalUri.getQuery() != null ? "?" + originalUri.getQuery() : ""));
        var newRequest = ClientRequest.from(request)
                .url(newUri)
                .build();
        log.info("Sending a request to {}:{}", newRequest.url().getHost(), newRequest.url().getPort());
        return next.exchange(newRequest);
    }

    private String getNextBaseUrl() {
        var index = counter.getAndUpdate(i -> (i + 1) % hosts.size());
        return hosts.get(index);
    }
}
