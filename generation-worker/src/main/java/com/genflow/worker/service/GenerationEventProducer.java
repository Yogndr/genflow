package com.genflow.worker.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class GenerationEventProducer {

    private static final String TOPIC = "generation-events";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public GenerationEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCompletedEvent(String message) {
        kafkaTemplate.send(TOPIC, message);

        System.out.println(
                "Kafka event published: " + message
        );
    }
}