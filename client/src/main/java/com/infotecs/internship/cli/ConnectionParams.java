package com.infotecs.internship.cli;

/**
 * Параметры подключения к SFTP-серверу, полученные из аргументов
 * командной строки.
 */
public final class ConnectionParams {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String remoteFilePath;

    public ConnectionParams(String host, int port, String username, String password, String remoteFilePath) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.remoteFilePath = remoteFilePath;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }
}