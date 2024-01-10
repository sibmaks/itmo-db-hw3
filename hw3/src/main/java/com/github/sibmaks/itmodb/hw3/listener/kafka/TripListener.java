package com.github.sibmaks.itmodb.hw3.listener.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sibmaks.itmodb.hw3.entity.TripEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TripListener {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;


    @KafkaListener(topics = "taxi_trip")
    public void onEvent(@Payload String payload,
                        @Header(name = "eventType")
                                String eventType,
                        Acknowledgment ack) throws JsonProcessingException {
        var eventBody = objectMapper.readValue(payload, TripEvent.class);
        var redisValueOperations = redisTemplate.opsForValue();
        var redisHashOperations = redisTemplate.opsForHash();
        if("start".equals(eventType)) {
            redisValueOperations.increment("ACTIVE_TRIPS");
            var locationId = eventBody.getPULocationID().toString();
            redisHashOperations.increment("ACTIVE_LOCAL_TRIPS", locationId, 1L);
        } else if("end".equals(eventType)) {
            redisValueOperations.decrement("ACTIVE_TRIPS");
            var locationId = eventBody.getDOLocationID().toString();
            redisHashOperations.increment("ACTIVE_LOCAL_TRIPS", locationId, -1L);
        } else {
            log.warn("Не известный тип события: {}", eventType);
        }
        ack.acknowledge();
    }

}
