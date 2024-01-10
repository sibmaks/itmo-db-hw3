package com.github.sibmaks.itmodb.hw3.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class DataImportTaskState {
    @Getter
    private final String taskId;
    private final AtomicInteger successProcessedRowsCount;
    private final AtomicInteger failedProcessedRowsCount;
    @Getter
    private final long startTime;
    @Getter
    @Setter
    private DataImportStatus status;
    @Getter
    private Long finishTime;


    public DataImportTaskState(String taskId) {
        this.taskId = taskId;
        this.status = DataImportStatus.CREATED;
        this.successProcessedRowsCount = new AtomicInteger();
        this.failedProcessedRowsCount = new AtomicInteger();
        this.startTime = System.currentTimeMillis();
    }

    public void incrementSuccessProcessedRowsCount() {
        successProcessedRowsCount.incrementAndGet();
    }

    public int getSuccessProcessedRowsCount() {
        return successProcessedRowsCount.get();
    }

    public void incrementFailedProcessedRowsCount() {
        failedProcessedRowsCount.incrementAndGet();
    }

    public int getFailedProcessedRowsCount() {
        return failedProcessedRowsCount.get();
    }

    public void finish() {
        if (finishTime != null) {
            throw new IllegalStateException("Task %s already finished".formatted(taskId));
        }
        finishTime = System.currentTimeMillis();
        status = DataImportStatus.FINISHED;
    }
}
