package com.infotecs.internship.json;

import com.infotecs.internship.model.AddressEntry;

import java.util.List;

/**
 * Сериализует список {@link AddressEntry} в JSON-подобную строку
 */
public class AddressJsonWriter {

    /**
     * @param entries список пар "домен - адрес" для сериализации, не {@code null}
     * @return JSON-строка с этим списком
     */
    public String write(List<AddressEntry> entries) {
        if (entries == null) {
            throw new IllegalArgumentException("entries must not be null");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n  \"addresses\": [");

        for (int i = 0; i < entries.size(); i++) {
            AddressEntry entry = entries.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("\n    {\n");
            sb.append("      \"domain\": \"").append(escape(entry.getDomain())).append("\",\n");
            sb.append("      \"ip\": \"").append(escape(entry.getIp())).append("\"\n");
            sb.append("    }");
        }

        if (!entries.isEmpty()) {
            sb.append("\n  ");
        }
        sb.append("]\n}");
        return sb.toString();
    }

    private String escape(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}