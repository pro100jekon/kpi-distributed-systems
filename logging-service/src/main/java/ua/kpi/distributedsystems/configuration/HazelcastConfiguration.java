package ua.kpi.distributedsystems.configuration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class HazelcastConfiguration {

    @Bean
    public IMap<UUID, String> map(HazelcastInstance instance) {
        return instance.getMap("logs");
    }
}
