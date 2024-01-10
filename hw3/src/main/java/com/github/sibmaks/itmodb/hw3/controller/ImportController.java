package com.github.sibmaks.itmodb.hw3.controller;

import com.github.sibmaks.itmodb.hw3.api.rq.ImportStateRq;
import com.github.sibmaks.itmodb.hw3.api.rs.ImportStateRs;
import com.github.sibmaks.itmodb.hw3.api.rq.StartImportRq;
import com.github.sibmaks.itmodb.hw3.api.rs.StartImportRs;
import com.github.sibmaks.itmodb.hw3.service.DataImportService;
import com.github.sibmaks.itmodb.hw3.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@RestController
@RequestMapping("/import-controller/")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ImportController {
    private final StorageService storageService;
    private final DataImportService dataImportService;


    @PostMapping("/start")
    public StartImportRs start(@RequestBody StartImportRq rq) {
        var resource = storageService.load(rq.getPath());
        var importTask = dataImportService.importResource(resource);
        return new StartImportRs(importTask.getTaskId());
    }

    @PostMapping("/state")
    public ImportStateRs state(@RequestBody ImportStateRq rq) {
        var taskState = dataImportService.getTaskState(rq.getTaskId());
        if(taskState == null) {
            throw new IllegalArgumentException("Процесс '%s' не известен".formatted(rq.getTaskId()));
        }
        return ImportStateRs.builder()
                .taskId(rq.getTaskId())
                .successProcessedRowsCount(taskState.getSuccessProcessedRowsCount())
                .failedProcessedRowsCount(taskState.getFailedProcessedRowsCount())
                .status(taskState.getStatus())
                .startTime(taskState.getStartTime())
                .finishTime(taskState.getFinishTime())
                .build();
    }

}
