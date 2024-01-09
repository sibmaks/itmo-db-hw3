package com.github.sibmaks.itmodb.hw3.service.storage;

import org.springframework.core.io.Resource;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public interface StorageService {

    /**
     * Загрузить ресурс из хранилища
     *
     * @param path путь до ресурса
     * @return экземпляр ресурса
     */
    Resource load(String path);

}
