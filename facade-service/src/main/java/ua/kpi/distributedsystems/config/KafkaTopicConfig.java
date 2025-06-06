package ua.kpi.distributedsystems.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    
    @Bean
    public NewTopic messagesTopic() {
        return TopicBuilder.name("messages")
                //.partitions(3)
                .partitions(1)
                .replicas(3)
                .build();
    }
}