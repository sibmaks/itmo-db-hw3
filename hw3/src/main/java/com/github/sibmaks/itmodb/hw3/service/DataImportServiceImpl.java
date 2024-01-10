package com.github.sibmaks.itmodb.hw3.service;

import com.github.sibmaks.itmodb.hw3.dto.DataImportStatus;
import com.github.sibmaks.itmodb.hw3.dto.DataImportTaskState;
import com.github.sibmaks.itmodb.hw3.entity.Trip;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@Slf4j
@Service
public class DataImportServiceImpl implements DataImportService {
    private final ExecutorService importExecutor;
    private final Map<String, DataImportTaskState> taskStates;
    private final MongoTemplate mongoTemplate;
    private final int bulkSize;

    public DataImportServiceImpl(@Value("${app.data-import.thread-pool-size}") int threadPoolSize,
                                 @Value("${app.data-import.bulk-size}") int bulkSize,
                                 MongoTemplate mongoTemplate) {
        this.importExecutor = Executors.newFixedThreadPool(threadPoolSize);
        this.mongoTemplate = mongoTemplate;
        this.taskStates = new ConcurrentHashMap<>();
        this.bulkSize = bulkSize;
    }

    @Override
    public DataImportTaskState importResource(Resource resource) {
        var taskId = UUID.randomUUID().toString();
        var dataImportTaskState = new DataImportTaskState(taskId);
        taskStates.put(taskId, dataImportTaskState);
        dataImportTaskState.setStatus(DataImportStatus.IN_QUEUE);
        importExecutor.execute(() -> processImport(resource, dataImportTaskState));
        return dataImportTaskState;
    }

    @Override
    public DataImportTaskState getTaskState(String taskId) {
        return taskStates.get(taskId);
    }

    private void processImport(Resource resource, DataImportTaskState dataImportTaskState) {
        dataImportTaskState.setStatus(DataImportStatus.IN_PROCESS);
        var taskId = dataImportTaskState.getTaskId();
        try (var inputStream = resource.getInputStream();
             var inputStreamReader = new InputStreamReader(inputStream);
             var bufferedReader = new BufferedReader(inputStreamReader);
             var csvReader = new CSVReader(bufferedReader)) {
            log.info("Задача импорта {} запущена", taskId);
            String[] line;
            try {
                var dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");

                var bulkInsertion = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Trip.class);

                var inserted = 0;

                while ((line = csvReader.readNext()) != null) {
                    try {

                        var pickUp = LocalDateTime.parse(line[1], dateTimeFormatter);
                        var dropOff = LocalDateTime.parse(line[2], dateTimeFormatter);

                        var trip = Trip.builder()
                                .uuid(UUID.randomUUID().toString())
                                .tripId(UUID.randomUUID().toString())
                                .vendorID(Integer.valueOf(line[0]))
                                .tpepPickupDatetime(pickUp)
                                .tpepPickupMillis(pickUp.toEpochSecond(ZoneOffset.UTC))
                                .tpepDropOffDatetime(dropOff)
                                .tpepDropOffMillis(dropOff.toEpochSecond(ZoneOffset.UTC))
                                .passengerCount(Integer.valueOf(line[3]))
                                .tripDistance(new BigDecimal(line[4]))
                                .rateCodeID(Integer.valueOf(line[5]))
                                .storeAndFwdFlag(line[6].charAt(0))
                                .PULocationID(Integer.valueOf(line[7]))
                                .DOLocationID(Integer.valueOf(line[8]))
                                .paymentType(Integer.valueOf(line[9]))
                                .fareAmount(new BigDecimal(line[10]))
                                .extra(new BigDecimal(line[11]))
                                .mtaTax(new BigDecimal(line[12]))
                                .tipAmount(new BigDecimal(line[13]))
                                .tollsAmount(new BigDecimal(line[14]))
                                .improvementSurcharge(new BigDecimal(line[15]))
                                .totalAmount(new BigDecimal(line[16]))
                                .build();

                        bulkInsertion.insert(trip);
                        inserted++;

                        if (inserted >= bulkSize) {
                            var start = Instant.now();

                            var bulkWriteResult = bulkInsertion.execute();

                            log.info("Пакетная вставка {} документов за {} мс", bulkWriteResult.getInsertedCount(), Duration.between(start, Instant.now()).toMillis());

                            bulkInsertion = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Trip.class);
                            inserted = 0;
                        }

                        dataImportTaskState.incrementSuccessProcessedRowsCount();
                    } catch (Exception e) {
                        log.error("Ошибка при обработки строчки %d в задаче %s".formatted(csvReader.getLinesRead(), taskId), e);
                        dataImportTaskState.incrementFailedProcessedRowsCount();
                    }
                }


                if (inserted > 0) {
                    var start = Instant.now();

                    var bulkWriteResult = bulkInsertion.execute();

                    log.info("Пакетная вставка {} документов за {} мс", bulkWriteResult.getInsertedCount(), Duration.between(start, Instant.now()).toMillis());
                }
            } catch (CsvValidationException e) {
                log.error("Ошибка при обработки строчки %d в задаче %s".formatted(csvReader.getLinesRead(), taskId), e);
                dataImportTaskState.incrementFailedProcessedRowsCount();
            }
            log.info("Задача импорта {} успешно завершена", taskId);
        } catch (IOException e) {
            log.error("Ошибка при выполнении задачи %s".formatted(taskId), e);
        } finally {
            dataImportTaskState.finish();
        }
    }
}
