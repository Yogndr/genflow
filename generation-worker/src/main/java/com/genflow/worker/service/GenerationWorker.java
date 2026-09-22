package com.genflow.worker.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class GenerationWorker {

    @RabbitListener(queues = "generation.queue")
    public void processGenerationJob(String message) {

        System.out.println(
                "Worker microservice processing job: " + message
        );
    }
}