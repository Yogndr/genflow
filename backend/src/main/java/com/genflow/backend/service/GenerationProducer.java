package com.genflow.backend.service;

import com.genflow.backend.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class GenerationProducer {

    private final RabbitTemplate rabbitTemplate;

    public GenerationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendGenerationJob(String message) {

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                message
        );

        System.out.println("Sent to RabbitMQ: " + message);
    }
}