package com.genflow.backend.service;

import com.genflow.backend.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class GenerationConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumeGenerationJob(String message) {

        System.out.println(
                "Received generation job: " + message
        );
    }
}