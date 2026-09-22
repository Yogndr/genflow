package com.genflow.backend.controller;

import com.genflow.backend.service.GenerationProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rabbitmq")
public class RabbitMQTestController {

    private final GenerationProducer generationProducer;

    public RabbitMQTestController(GenerationProducer generationProducer) {
        this.generationProducer = generationProducer;
    }

    @PostMapping("/publish")
    public ResponseEntity<String> publish(
            @RequestParam String message) {

        generationProducer.sendGenerationJob(message);

        return ResponseEntity.ok(
                "Generation job sent to RabbitMQ"
        );
    }
}