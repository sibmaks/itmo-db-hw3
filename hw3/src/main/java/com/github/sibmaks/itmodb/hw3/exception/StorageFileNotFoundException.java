package com.github.sibmaks.itmodb.hw3.exception;

/**
 * @author sibmaks
 * @since 0.0.1
 */
public class StorageFileNotFoundException extends RuntimeException {

    public StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
