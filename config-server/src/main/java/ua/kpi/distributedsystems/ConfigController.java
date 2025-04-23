package ua.kpi.distributedsystems;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ua.kpi.config.ServiceUrlConfiguration;

import java.util.List;

@RestController
public class ConfigController {

    private final ServiceUrlConfiguration.ServiceUrlProperties properties;

    public ConfigController(ServiceUrlConfiguration.ServiceUrlProperties properties) {
        this.properties = properties;
    }

    @GetMapping(value = "/{service-name}", produces = "application/json")
    public Mono<List<String>> getConfig(@PathVariable("service-name") String serviceName) {
        return Mono.just(properties.urls().getOrDefault(serviceName, List.of()));
    }
}
