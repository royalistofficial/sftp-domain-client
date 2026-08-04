package com.infotecs.internship.model;

import java.util.Objects;

/**
 * Неизменяемая пара "домен - IP-адрес".
 */
public final class AddressEntry {

    private final String domain;
    private final String ip;

    /**
     * @param domain доменное имя, не может быть {@code null} или пустым
     * @param ip     IP-адрес в виде строки, не может быть {@code null} или пустым
     * @throws IllegalArgumentException если domain или ip {@code null}/пустые
     */
    public AddressEntry(String domain, String ip) {
        if (domain == null || domain.trim().isEmpty()) {
            throw new IllegalArgumentException("Domain must not be null or empty");
        }
        if (ip == null || ip.trim().isEmpty()) {
            throw new IllegalArgumentException("IP must not be null or empty");
        }
        this.domain = domain;
        this.ip = ip;
    }

    /** @return доменное имя */
    public String getDomain() {
        return domain;
    }

    /** @return IP-адрес */
    public String getIp() {
        return ip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AddressEntry)) {
            return false;
        }
        AddressEntry that = (AddressEntry) o;
        return domain.equals(that.domain) && ip.equals(that.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(domain, ip);
    }

    @Override
    public String toString() {
        return "AddressEntry{domain='" + domain + "', ip='" + ip + "'}";
    }
}