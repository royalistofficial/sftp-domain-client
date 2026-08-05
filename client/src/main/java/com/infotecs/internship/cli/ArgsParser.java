package com.infotecs.internship.cli;

/**
 * Разбирает аргументы командной строки в {@link ConnectionParams}.
 */
public class ArgsParser {

    private static final String DEFAULT_REMOTE_FILE_PATH = "addresses.json";
    private static final int MIN_ARGS = 4;
    private static final int MAX_ARGS = 5;
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    /**
     * @param args аргументы командной строки
     * @return разобранные параметры подключения
     * @throws IllegalArgumentException если количество аргументов не
     *                                  соответствует ожидаемому формату
     *                                  или порт не является корректным числом
     */
    public ConnectionParams parse(String[] args) {
        if (args == null || args.length < MIN_ARGS || args.length > MAX_ARGS) {
            throw new IllegalArgumentException(
                    "Usage: <host> <port> <username> <password> [remoteFilePath]");
        }

        String host = args[0];
        int port = parsePort(args[1]);
        String username = args[2];
        String password = args[3];
        String remoteFilePath = args.length == MAX_ARGS ? args[4] : DEFAULT_REMOTE_FILE_PATH;

        return new ConnectionParams(host, port, username, password, remoteFilePath);
    }

    private int parsePort(String value) {
        int port;
        try {
            port = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Port must be a number: " + value, e);
        }
        if (port < MIN_PORT || port > MAX_PORT) {
            throw new IllegalArgumentException(
                    "Port must be between " + MIN_PORT + " and " + MAX_PORT + ": " + port);
        }
        return port;
    }
}