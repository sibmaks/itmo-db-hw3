package com.github.sibmaks.itmodb.hw3.service.storage;

import com.github.sibmaks.itmodb.hw3.exception.StorageFileNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.nio.file.Path;

/**
 * @author sibmaks
 * @since 0.0.1
 */
@Service
public class StorageServiceImpl implements StorageService {
    private final Path rootPath;

    public StorageServiceImpl(
            @Value("${app.storage.root-path}") Path rootPath
    ) {
        this.rootPath = rootPath;
    }

    @Override
    public Resource load(String path) {
        try {
            var file = resolve(path);
            var resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Ошибка чтения файла: " + path);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Ошибка чтения файла: " + path, e);
        }
    }

    private Path resolve(String filename) {
        return rootPath.resolve(filename);
    }
}
