package com.infotecs.internship.validation;

/**
 * Контракт для валидации IP-адресов.
 */
public interface IpValidator {

    /**
     * Проверяет корректность переданной строки как IP-адреса.
     *
     * @param ip строка для проверки, может быть {@code null}
     * @return {@code true}, если строка является валидным IP-адресом
     */
    boolean isValid(String ip);
}