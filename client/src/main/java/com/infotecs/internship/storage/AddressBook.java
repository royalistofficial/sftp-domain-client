package com.infotecs.internship.storage;

import com.infotecs.internship.model.AddressEntry;
import com.infotecs.internship.validation.IpValidator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Хранит пары "домен - IP" в памяти: гарантирует уникальность домена и IP,
 * обеспечивает поиск в обе стороны, добавление и удаление записей.
 */
public class AddressBook {

    private final IpValidator ipValidator;
    private final Map<String, String> domainToIp = new LinkedHashMap<>();
    private final Map<String, String> ipToDomain = new LinkedHashMap<>();

    /**
     * @param initialEntries начальный список записей; домены и IP должны
     *                       быть уникальны между собой
     * @param ipValidator    валидатор для проверки формата IP при добавлении
     *                       новых записей
     * @throws DuplicateEntryException если в {@code initialEntries} есть
     *                                 повторяющийся домен или IP
     */
    public AddressBook(List<AddressEntry> initialEntries, IpValidator ipValidator) {
        this.ipValidator = ipValidator;
        for (AddressEntry entry : initialEntries) {
            putUnique(entry.getDomain(), entry.getIp());
        }
    }

    /** @return список всех записей, отсортированный по алфавиту домена */
    public List<AddressEntry> listSortedByDomain() {
        List<AddressEntry> result = getAllEntries();
        result.sort(Comparator.comparing(AddressEntry::getDomain));
        return result;
    }

    /**
     * @param domain доменное имя
     * @return IP-адрес, если домен найден, иначе {@link Optional#empty()}
     */
    public Optional<String> findIpByDomain(String domain) {
        return Optional.ofNullable(domainToIp.get(domain));
    }

    /**
     * @param ip IP-адрес
     * @return доменное имя, если IP найден, иначе {@link Optional#empty()}
     */
    public Optional<String> findDomainByIp(String ip) {
        return Optional.ofNullable(ipToDomain.get(ip));
    }

    /**
     * Добавляет новую пару "домен - IP".
     *
     * @param domain доменное имя, не должно совпадать с уже существующим
     * @param ip     IPv4-адрес, должен пройти валидацию формата и не
     *               совпадать с уже существующим
     * @throws IllegalArgumentException если домен/IP пустые либо IP не
     *                                  проходит валидацию формата
     * @throws DuplicateEntryException  если домен или IP уже есть в хранилище
     */
    public void add(String domain, String ip) {
        AddressEntry entry = new AddressEntry(domain, ip); // проверка на null/пустоту
        if (!ipValidator.isValid(entry.getIp())) {
            throw new IllegalArgumentException("Invalid IPv4 address: " + ip);
        }
        putUnique(entry.getDomain(), entry.getIp());
    }

    /**
     * Удаляет запись по доменному имени или IP-адресу.
     *
     * @param domainOrIp доменное имя или IP-адрес искомой записи
     * @return {@code true}, если запись была найдена и удалена,
     *         {@code false}, если ни домен, ни IP не найдены
     */
    public boolean remove(String domainOrIp) {
        String domain = domainToIp.containsKey(domainOrIp) ? domainOrIp : ipToDomain.get(domainOrIp);
        if (domain == null) {
            return false;
        }
        String ip = domainToIp.remove(domain);
        ipToDomain.remove(ip);
        return true;
    }

    /** @return независимая копия текущего списка всех записей */
    public List<AddressEntry> getAllEntries() {
        List<AddressEntry> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : domainToIp.entrySet()) {
            result.add(new AddressEntry(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private void putUnique(String domain, String ip) {
        if (domainToIp.containsKey(domain)) {
            throw new DuplicateEntryException("Domain already exists: " + domain);
        }
        if (ipToDomain.containsKey(ip)) {
            throw new DuplicateEntryException("IP already exists: " + ip);
        }
        domainToIp.put(domain, ip);
        ipToDomain.put(ip, domain);
    }
}