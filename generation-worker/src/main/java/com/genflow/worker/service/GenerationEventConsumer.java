package com.genflow.worker.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class GenerationEventConsumer {

    @KafkaListener(
        topics = "generation-events",
        groupId = "genflow-group"
    )
    public void consumeGenerationEvent(String message) {

        System.out.println(
            "Kafka event consumed: " + message
        );
    }
}