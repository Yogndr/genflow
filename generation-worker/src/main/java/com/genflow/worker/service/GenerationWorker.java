package com.genflow.worker.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class GenerationWorker {

    private final GenerationEventProducer eventProducer;

    public GenerationWorker(GenerationEventProducer eventProducer) {
        this.eventProducer = eventProducer;
    }

    @RabbitListener(queues = "generation.queue")
    public void processGenerationJob(String message) {

        System.out.println(
                "Worker microservice processing job: " + message
        );

        eventProducer.publishCompletedEvent(
                "GENERATION_COMPLETED: " + message
        );
    }
}