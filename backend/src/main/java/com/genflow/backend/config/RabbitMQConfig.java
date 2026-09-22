package com.genflow.backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE = "generation.queue";
    public static final String EXCHANGE = "generation.exchange";
    public static final String ROUTING_KEY = "generation.routing.key";

    @Bean
    public Queue generationQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public DirectExchange generationExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding generationBinding(
            Queue generationQueue,
            DirectExchange generationExchange) {

        return BindingBuilder
                .bind(generationQueue)
                .to(generationExchange)
                .with(ROUTING_KEY);
    }
}