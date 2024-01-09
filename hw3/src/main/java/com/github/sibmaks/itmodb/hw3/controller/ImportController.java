package com.github.sibmaks.itmodb.hw3.controller;

import com.github.sibmaks.itmodb.hw3.api.rq.StartImportRq;
import com.github.sibmaks.itmodb.hw3.api.rq.StartImportRs;
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

}
