package com.infotecs.internship.transport;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Реализация {@link FileTransport} для работы с файлом на SFTP-сервере.
 */
public class SftpFileTransport implements FileTransport {

    private static final int CONNECT_TIMEOUT_MS = 10_000;

    /** Путь к known_hosts по умолчанию - как у стандартного OpenSSH-клиента. */
    private static final String DEFAULT_KNOWN_HOSTS_PATH =
            System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "known_hosts";

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String remoteFilePath;
    private final String knownHostsPath;

    private Session session;
    private ChannelSftp channel;

    /**
     * Создаёт транспорт, использующий known_hosts по умолчанию
     * ({@code ~/.ssh/known_hosts}).
     *
     * @param host           адрес SFTP-сервера
     * @param port           порт SFTP-сервера
     * @param username       логин
     * @param password       пароль
     * @param remoteFilePath путь к файлу с адресами на сервере
     */
    public SftpFileTransport(String host, int port, String username, String password, String remoteFilePath) {
        this(host, port, username, password, remoteFilePath, DEFAULT_KNOWN_HOSTS_PATH);
    }

    /**
     * @param host           адрес SFTP-сервера
     * @param port           порт SFTP-сервера
     * @param username       логин
     * @param password       пароль
     * @param remoteFilePath путь к файлу с адресами на сервере
     * @param knownHostsPath путь к файлу known_hosts, используемому для проверки
     *                       отпечатка хоста при подключении
     */
    public SftpFileTransport(String host, int port, String username, String password,
                              String remoteFilePath, String knownHostsPath) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.remoteFilePath = remoteFilePath;
        this.knownHostsPath = knownHostsPath;
    }

    /**
     * Устанавливает соединение с SFTP-сервером и открывает sftp-канал.
     *
     * <p>Отпечаток сервера сверяется с файлом known_hosts
     * ({@link #getKnownHostsPath()}) при строгой проверке
     * ({@code StrictHostKeyChecking=yes}). Если отпечаток отсутствует или
     * не совпадает с ожидаемым, подключение прерывается.</p>
     *
     * @throws IOException если файл known_hosts не удалось прочитать,
     *                      отпечаток сервера не прошёл проверку, либо
     *                      подключение/аутентификация не удались
     */
    public void connect() throws IOException {
        JSch jsch = new JSch();
        try {
            jsch.setKnownHosts(knownHostsPath);
        } catch (JSchException e) {
            throw new IOException("Не удалось загрузить known_hosts файл: " + knownHostsPath
                    + ". Убедитесь, что файл существует и доступен для чтения.", e);
        }

        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "yes");
            session.connect(CONNECT_TIMEOUT_MS);

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect(CONNECT_TIMEOUT_MS);
        } catch (JSchException e) {
            throw wrapConnectException(e);
        }
    }

    private IOException wrapConnectException(JSchException e) {
        String message = e.getMessage();
        if (message != null && message.contains("HostKey")) {
            return new IOException("Проверка отпечатка хоста " + host + ":" + port + " не пройдена: "
                    + message + ". Если это ожидаемый сервер, добавьте его доверенный отпечаток в "
                    + knownHostsPath + " (например, командой 'ssh-keyscan -p " + port + " " + host
                    + " >> " + knownHostsPath + "' - но только после проверки отпечатка по доверенному "
                    + "каналу, а не автоматически).", e);
        }
        return new IOException("Failed to connect to SFTP server " + host + ":" + port, e);
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

    /** @return путь к файлу known_hosts, используемому для проверки отпечатка хоста */
    public String getKnownHostsPath() {
        return knownHostsPath;
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