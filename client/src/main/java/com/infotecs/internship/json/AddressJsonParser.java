package com.infotecs.internship.json;

import com.infotecs.internship.model.AddressEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Разбирает JSON-подобный файл вида
 * {@code {"addresses": [{"domain": "...", "ip": "..."}]}} в список
 * {@link AddressEntry}.
 */
public class AddressJsonParser {

    private static final String ADDRESSES_KEY = "addresses";
    private static final String DOMAIN_KEY = "domain";
    private static final String IP_KEY = "ip";

    /**
     * @param json содержимое файла в виде строки
     * @return список пар "домен - адрес" в том порядке, в котором они
     *         встречаются в файле
     * @throws JsonParseException если JSON синтаксически некорректен,
     *         отсутствует массив {@code addresses} или у какой-либо записи
     *         отсутствует/некорректно поле {@code domain} или {@code ip}
     */
    public List<AddressEntry> parse(String json) {
        if (json == null) {
            throw new JsonParseException("JSON content must not be null");
        }

        Object root = parseRoot(json);
        Map<?, ?> rootMap = asObject(root, "Root JSON element must be an object");
        List<?> addressesList = asArray(rootMap.get(ADDRESSES_KEY), "Missing or invalid '" + ADDRESSES_KEY + "' array");

        List<AddressEntry> result = new ArrayList<>();
        int index = 0;
        for (Object item : addressesList) {
            result.add(parseEntry(item, index));
            index++;
        }
        return result;
    }

    private Object parseRoot(String json) {
        try {
            return new JsonValueParser(json).parse();
        } catch (JsonValueParser.SyntaxException e) {
            throw new JsonParseException("Invalid JSON syntax: " + e.getMessage(), e);
        }
    }

    private AddressEntry parseEntry(Object item, int index) {
        Map<?, ?> entryMap = asObject(item, "Element at index " + index + " of '" + ADDRESSES_KEY + "' must be an object");

        Object domainValue = entryMap.get(DOMAIN_KEY);
        Object ipValue = entryMap.get(IP_KEY);

        if (!(domainValue instanceof String)) {
            throw new JsonParseException("Missing or invalid '" + DOMAIN_KEY + "' field at index " + index);
        }
        if (!(ipValue instanceof String)) {
            throw new JsonParseException("Missing or invalid '" + IP_KEY + "' field at index " + index);
        }

        try {
            return new AddressEntry((String) domainValue, (String) ipValue);
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Invalid entry at index " + index + ": " + e.getMessage(), e);
        }
    }

    private Map<?, ?> asObject(Object value, String errorMessage) {
        if (!(value instanceof Map)) {
            throw new JsonParseException(errorMessage);
        }
        return (Map<?, ?>) value;
    }

    private List<?> asArray(Object value, String errorMessage) {
        if (!(value instanceof List)) {
            throw new JsonParseException(errorMessage);
        }
        return (List<?>) value;
    }
}