package com.genflow.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class GenerationWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GenerationWorkerApplication.class, args);
	}

}
