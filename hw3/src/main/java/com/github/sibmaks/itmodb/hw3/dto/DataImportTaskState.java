package com.github.sibmaks.itmodb.hw3.dto;

import lombok.Getter;

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
    private long finishTime;


    public DataImportTaskState(String taskId) {
        this.taskId = taskId;
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
        if (finishTime != 0) {
            throw new IllegalStateException("Task %s already finished".formatted(taskId));
        }
        finishTime = System.currentTimeMillis();
    }
}
