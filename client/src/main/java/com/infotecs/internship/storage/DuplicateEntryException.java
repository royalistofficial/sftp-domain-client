package com.infotecs.internship.storage;

/**
 * Выбрасывается при попытке добавить домен или IP-адрес,
 * который уже присутствует в хранилище.
 */
public class DuplicateEntryException extends RuntimeException {

    public DuplicateEntryException(String message) {
        super(message);
    }
}