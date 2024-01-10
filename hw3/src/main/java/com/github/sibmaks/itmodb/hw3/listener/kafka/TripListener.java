package com.github.sibmaks.itmodb.hw3.listener.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sibmaks.itmodb.hw3.entity.TripEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TripListener {
    @Value("${app.trip.redis.idempotence.ttl.value}")
    private long ttlValue;
    @Value("${app.trip.redis.idempotence.ttl.unit}")
    private TimeUnit ttlUnit;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "taxi_trip")
    public void onEvent(@Payload String payload,
                        @Header(name = "eventId")
                        String eventId,
                        @Header(name = "eventType")
                        String eventType,
                        Acknowledgment ack) throws JsonProcessingException {
        var redisValueOperations = redisTemplate.opsForValue();
        if (redisValueOperations.get(eventId) != null) {
            log.warn("Событие уже обработано: {}", eventId);
            ack.acknowledge();
            return;
        }
        proceed(payload, eventType);
        redisValueOperations.set(eventId, "true", ttlValue, ttlUnit);
        ack.acknowledge();
    }

    private void proceed(String payload, String eventType) throws JsonProcessingException {
        var redisValueOperations = redisTemplate.opsForValue();
        var redisHashOperations = redisTemplate.opsForHash();

        var eventBody = objectMapper.readValue(payload, TripEvent.class);
        if ("start".equals(eventType)) {
            redisValueOperations.increment("ACTIVE_TRIPS");
            var locationId = eventBody.getPULocationID().toString();
            redisHashOperations.increment("ACTIVE_LOCAL_TRIPS", locationId, 1L);
        } else if ("end".equals(eventType)) {
            redisValueOperations.decrement("ACTIVE_TRIPS");
            var locationId = eventBody.getDOLocationID().toString();
            redisHashOperations.increment("ACTIVE_LOCAL_TRIPS", locationId, -1L);
        } else {
            log.warn("Не известный тип события: {}", eventType);
        }
    }

}
