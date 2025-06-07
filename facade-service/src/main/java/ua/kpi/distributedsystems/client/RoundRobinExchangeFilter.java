package ua.kpi.distributedsystems.client;

import com.ecwid.consul.v1.health.model.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulServiceInstance;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinExchangeFilter implements ExchangeFilterFunction {

    private static final Logger log = LoggerFactory.getLogger(RoundRobinExchangeFilter.class);

    private final AtomicInteger counter = new AtomicInteger(0);
    private int lastSize;
    private final DiscoveryClient discoveryClient;
    private final String serviceId;

    public RoundRobinExchangeFilter(DiscoveryClient discoveryClient, String serviceId) {
        this.discoveryClient = discoveryClient;
        this.serviceId = serviceId;
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
        var hosts = discoveryClient.getInstances(serviceId).stream()
                .filter(this::filter)
                .toList();
        if (lastSize != hosts.size()) {
            lastSize = hosts.size();
            counter.set(0);
        }
        var index = counter.getAndUpdate(i -> (i + 1) % hosts.size());
        return hosts.get(index).getUri().toString();
    }

    private boolean filter(ServiceInstance instance) {
        return ((ConsulServiceInstance) instance).getHealthService().getChecks()
                .stream()
                .anyMatch(i -> i.getStatus().equals(Check.CheckStatus.PASSING)
                        && !i.getCheckId().equals("serfHealth"));
    }
}
