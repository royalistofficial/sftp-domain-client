package com.infotecs.internship.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Парсер JSON.
 */
class JsonValueParser {

    /** Ошибка синтаксического разбора JSON. */
    static class SyntaxException extends RuntimeException {
        SyntaxException(String message) {
            super(message);
        }
    }

    private final String text;
    private int pos;

    JsonValueParser(String text) {
        this.text = text;
        this.pos = 0;
    }

    /** @return разобранное дерево значений (Map/List/String/Number/Boolean/null) */
    Object parse() {
        skipWhitespace();
        Object value = parseValue();
        skipWhitespace();
        if (pos != text.length()) {
            throw new SyntaxException("Unexpected trailing content at position " + pos);
        }
        return value;
    }

    private Object parseValue() {
        if (pos >= text.length()) {
            throw new SyntaxException("Unexpected end of input");
        }
        char c = text.charAt(pos);
        switch (c) {
            case '{':
                return parseObject();
            case '[':
                return parseArray();
            case '"':
                return parseString();
            case 't':
                return parseLiteral("true", Boolean.TRUE);
            case 'f':
                return parseLiteral("false", Boolean.FALSE);
            case 'n':
                return parseLiteral("null", null);
            default:
                return parseNumber();
        }
    }

    private Map<String, Object> parseObject() {
        expect('{');
        Map<String, Object> result = new LinkedHashMap<>();
        skipWhitespace();
        if (peek() == '}') {
            pos++;
            return result;
        }
        while (true) {
            skipWhitespace();
            if (peek() != '"') {
                throw new SyntaxException("Expected string key at position " + pos);
            }
            String key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            Object value = parseValue();
            result.put(key, value);
            skipWhitespace();
            char next = peek();
            if (next == ',') {
                pos++;
                continue;
            }
            if (next == '}') {
                pos++;
                break;
            }
            throw new SyntaxException("Expected ',' or '}' at position " + pos);
        }
        return result;
    }

    private List<Object> parseArray() {
        expect('[');
        List<Object> result = new ArrayList<>();
        skipWhitespace();
        if (peek() == ']') {
            pos++;
            return result;
        }
        while (true) {
            skipWhitespace();
            result.add(parseValue());
            skipWhitespace();
            char next = peek();
            if (next == ',') {
                pos++;
                continue;
            }
            if (next == ']') {
                pos++;
                break;
            }
            throw new SyntaxException("Expected ',' or ']' at position " + pos);
        }
        return result;
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= text.length()) {
                throw new SyntaxException("Unterminated string");
            }
            char c = text.charAt(pos++);
            if (c == '"') {
                break;
            }
            if (c == '\\') {
                sb.append(parseEscapeSequence());
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private char parseEscapeSequence() {
        if (pos >= text.length()) {
            throw new SyntaxException("Unterminated escape sequence");
        }
        char esc = text.charAt(pos++);
        switch (esc) {
            case '"':
                return '"';
            case '\\':
                return '\\';
            case '/':
                return '/';
            case 'b':
                return '\b';
            case 'f':
                return '\f';
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case 'u':
                return parseUnicodeEscape();
            default:
                throw new SyntaxException("Invalid escape character: \\" + esc);
        }
    }

    private char parseUnicodeEscape() {
        if (pos + 4 > text.length()) {
            throw new SyntaxException("Invalid unicode escape at position " + pos);
        }
        String hex = text.substring(pos, pos + 4);
        try {
            char result = (char) Integer.parseInt(hex, 16);
            pos += 4;
            return result;
        } catch (NumberFormatException e) {
            throw new SyntaxException("Invalid unicode escape: \\u" + hex);
        }
    }

    private Object parseLiteral(String literal, Object value) {
        if (pos + literal.length() > text.length() || !text.startsWith(literal, pos)) {
            throw new SyntaxException("Invalid literal at position " + pos);
        }
        pos += literal.length();
        return value;
    }

    private Object parseNumber() {
        int start = pos;
        if (peek() == '-') {
            pos++;
        }
        if (pos >= text.length() || !Character.isDigit(text.charAt(pos))) {
            throw new SyntaxException("Invalid number at position " + start);
        }
        while (pos < text.length() && Character.isDigit(text.charAt(pos))) {
            pos++;
        }
        boolean isDouble = false;
        if (pos < text.length() && text.charAt(pos) == '.') {
            isDouble = true;
            pos++;
            while (pos < text.length() && Character.isDigit(text.charAt(pos))) {
                pos++;
            }
        }
        if (pos < text.length() && (text.charAt(pos) == 'e' || text.charAt(pos) == 'E')) {
            isDouble = true;
            pos++;
            if (pos < text.length() && (text.charAt(pos) == '+' || text.charAt(pos) == '-')) {
                pos++;
            }
            while (pos < text.length() && Character.isDigit(text.charAt(pos))) {
                pos++;
            }
        }
        String numberStr = text.substring(start, pos);
        return isDouble ? (Object) Double.parseDouble(numberStr) : (Object) Long.parseLong(numberStr);
    }

    private void expect(char expected) {
        if (pos >= text.length() || text.charAt(pos) != expected) {
            throw new SyntaxException("Expected '" + expected + "' at position " + pos);
        }
        pos++;
    }

    private char peek() {
        if (pos >= text.length()) {
            throw new SyntaxException("Unexpected end of input");
        }
        return text.charAt(pos);
    }

    private void skipWhitespace() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
    }
}