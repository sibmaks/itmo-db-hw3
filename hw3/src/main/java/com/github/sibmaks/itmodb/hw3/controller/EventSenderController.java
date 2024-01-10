package com.github.sibmaks.itmodb.hw3.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.sibmaks.itmodb.hw3.entity.TripEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@Slf4j
@RestController
@RequestMapping("/event-sender-controller/")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventSenderController {
    private final MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;


    @PostMapping("/send")
    public void send() throws JsonProcessingException {
        var query = new Query()
                .with(Sort.by(new Sort.Order(Sort.Direction.ASC, "eventTime")));

        var objectMapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .registerModules(new JavaTimeModule());

        for (var tripEvent : mongoTemplate.find(query, TripEvent.class)) {
            var tripId = UUID.randomUUID().toString();
            var payload = objectMapper.writeValueAsString(tripEvent);
            var headers = new RecordHeaders();
            headers.add("eventId", tripId.getBytes(StandardCharsets.UTF_8));
            headers.add("eventType", tripEvent.getEventType().getBytes(StandardCharsets.UTF_8));
            headers.add("eventTime", Long.toString(tripEvent.getEventTime()).getBytes(StandardCharsets.UTF_8));
            var message = new ProducerRecord<>("taxi_trip", null, tripId, payload, headers);
            kafkaTemplate.send(message);
        }
    }

}
