package com.github.sibmaks.itmodb.hw3.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trips")
@ToString
public class Trip {
    @Id
    private String uuid;
    private String tripId;
    private Integer vendorID;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime tpepPickupDatetime;
    private Long tpepPickupMillis;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime tpepDropOffDatetime;
    private Long tpepDropOffMillis;
    private Integer passengerCount;
    private BigDecimal tripDistance;
    private Integer rateCodeID;
    private Character storeAndFwdFlag;
    private Integer PULocationID;
    private Integer DOLocationID;
    private Integer paymentType;
    private BigDecimal fareAmount;
    private BigDecimal extra;
    private BigDecimal mtaTax;
    private BigDecimal tipAmount;
    private BigDecimal tollsAmount;
    private BigDecimal improvementSurcharge;
    private BigDecimal totalAmount;
}
