package com.infotecs.internship.transport;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Реализация {@link FileTransport} для работы с файлом на SFTP-сервере
 */
public class SftpFileTransport implements FileTransport {

    private static final int CONNECT_TIMEOUT_MS = 10_000;

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String remoteFilePath;

    private Session session;
    private ChannelSftp channel;

    /**
     * @param host           адрес SFTP-сервера
     * @param port           порт SFTP-сервера
     * @param username       логин
     * @param password       пароль
     * @param remoteFilePath путь к файлу с адресами на сервере
     */
    public SftpFileTransport(String host, int port, String username, String password, String remoteFilePath) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.remoteFilePath = remoteFilePath;
    }

    /**
     * Устанавливает соединение с SFTP-сервером и открывает sftp-канал.
     *
     * @throws IOException если подключение или аутентификация не удались
     */
    public void connect() throws IOException {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            // упрощение для тестового задания: не проверяем host key.
            // В продакшене здесь должна быть настроена проверка known_hosts.
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(CONNECT_TIMEOUT_MS);

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(CONNECT_TIMEOUT_MS);
        } catch (JSchException e) {
            throw new IOException("Failed to connect to SFTP server " + host + ":" + port, e);
        }
    }

    /** Закрывает sftp-канал и сессию. Безопасно вызывать даже без активного соединения. */
    public void disconnect() {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    /** @return {@code true}, если соединение установлено и канал открыт */
    public boolean isConnected() {
        return channel != null && channel.isConnected();
    }

    @Override
    public String download() throws IOException {
        ensureConnected();
        try (InputStream in = channel.get(remoteFilePath);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        } catch (SftpException e) {
            throw new IOException("Failed to download file " + remoteFilePath, e);
        }
    }

    @Override
    public void upload(String content) throws IOException {
        ensureConnected();
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            channel.put(in, remoteFilePath);
        } catch (SftpException e) {
            throw new IOException("Failed to upload file " + remoteFilePath, e);
        }
    }

    private void ensureConnected() throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected to SFTP server. Call connect() first.");
        }
    }
}