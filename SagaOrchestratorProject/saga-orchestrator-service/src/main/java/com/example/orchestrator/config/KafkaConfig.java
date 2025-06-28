package com.example.orchestrator.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

// This class creates all necessary Kafka topics when the application starts.
@Configuration
public class KafkaConfig {

    // Topics for sending commands to services
    @Bean public NewTopic paymentStepTopic() { return TopicBuilder.name("saga.step.payment").partitions(1).build(); }
    @Bean public NewTopic inventoryStepTopic() { return TopicBuilder.name("saga.step.inventory").partitions(1).build(); }
    @Bean public NewTopic notificationStepTopic() { return TopicBuilder.name("saga.step.notification").partitions(1).build(); }

    // Topics for receiving replies from services
    @Bean public NewTopic paymentReplyTopic() { return TopicBuilder.name("saga.reply.payment").partitions(1).build(); }
    @Bean public NewTopic inventoryReplyTopic() { return TopicBuilder.name("saga.reply.inventory").partitions(1).build(); }
    @Bean public NewTopic notificationReplyTopic() { return TopicBuilder.name("saga.reply.notification").partitions(1).build(); }

    // Topics for sending compensation (rollback) commands
    @Bean public NewTopic paymentCompensationTopic() { return TopicBuilder.name("saga.compensate.payment").partitions(1).build(); }
    @Bean public NewTopic inventoryCompensationTopic() { return TopicBuilder.name("saga.compensate.inventory").partitions(1).build(); }
}