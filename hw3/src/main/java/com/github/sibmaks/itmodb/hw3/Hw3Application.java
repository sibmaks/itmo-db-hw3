package com.github.sibmaks.itmodb.hw3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class Hw3Application {

	public static void main(String[] args) {
		SpringApplication.run(Hw3Application.class, args);
	}

}
