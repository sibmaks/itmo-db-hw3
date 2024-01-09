package com.github.sibmaks.itmodb.hw3.service;

import com.github.sibmaks.itmodb.hw3.dto.DataImportTaskState;
import org.springframework.core.io.Resource;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public interface DataImportService {

    /**
     * Запустить импорт ресурса
     * @param resource ресурс с данными
     * @return идентификатор процесса импорта
     */
    DataImportTaskState importResource(Resource resource);


}
