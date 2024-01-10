package com.github.sibmaks.itmodb.hw3.controller;

import com.github.sibmaks.itmodb.hw3.entity.Trip;
import com.github.sibmaks.itmodb.hw3.entity.TripEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.LiteralOperators;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@Slf4j
@RestController
@RequestMapping("/event-controller/")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class EventCreatorController {
    private final MongoTemplate mongoTemplate;


    @PostMapping("/create")
    public String create() {
        var filterTrips = match(
                Criteria.expr(
                        ComparisonOperators.Gt.valueOf("tpepDropOffMillis")
                                .greaterThan(ArithmeticOperators.Add.valueOf("tpepPickupMillis").add(60000))
                )

        );

        // Создание документа для начала поездки
        var startTripProjection = project()
                .and("_id").as("tripId")
                .and(LiteralOperators.Literal.asLiteral("start")).as("eventType")
                .and("tpepPickupMillis").as("eventTime")
                .andInclude("VendorID", "tpepPickupDatetime", "tpepPickupMillis", "PULocationID")
                .andExclude("_id");

        // Создание документа для окончания поездки
        var endTripProjection = project()
                .and("_id").as("tripId")
                .and(LiteralOperators.Literal.asLiteral("end")).as("eventType")
                .and("tpepDropOffMillis").as("eventTime")
                .andInclude(
                        "tpepDropOffDatetime",
                        "tpepDropOffMillis",
                        "passengerCount",
                        "tripDistance",
                        "rateCodeID",
                        "storeAndFwdFlag",
                        "DOLocationID",
                        "paymentType",
                        "fareAmount",
                        "extra",
                        "mtaTax",
                        "tipAmount",
                        "tollsAmount",
                        "improvementSurcharge",
                        "totalAmount"
                )
                .andExclude("_id");

        // Объединение документов
        var facet = facet(startTripProjection).as("startTrips")
                .and(endTripProjection)
                .as("endTrips");

        var combineTrips = project().andExpression("concatArrays(startTrips, endTrips)").as("allEvents");
        var unwindAllTrips = unwind("$allEvents");
        var replaceRoot = replaceRoot("$allEvents");
        var output = out("trip_events");

        // Сборка и выполнение запроса
        var aggregation = newAggregation(
                filterTrips,
                facet,
                combineTrips,
                unwindAllTrips,
                replaceRoot,
                output
        );

        var results = mongoTemplate.aggregate(aggregation, Trip.class, TripEvent.class);

        // Добавление индекса
        mongoTemplate.indexOps(TripEvent.class)
                .ensureIndex(new Index().on("eventTime", Sort.Direction.ASC));

        return results.getServerUsed();
    }

}
