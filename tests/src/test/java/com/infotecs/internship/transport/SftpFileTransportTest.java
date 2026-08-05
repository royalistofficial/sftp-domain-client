package com.infotecs.internship.transport;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Тесты для {@link SftpFileTransport}.
 */
public class SftpFileTransportTest {

    private SftpFileTransport transport;

    @BeforeMethod
    public void setUp() {
        transport = new SftpFileTransport("localhost", 22, "user", "password", "/remote/addresses.json");
    }
    @Test
    public void newInstanceIsNotConnected() {
        Assert.assertFalse(transport.isConnected());
    }

    @Test(expectedExceptions = IOException.class)
    public void downloadThrowsWhenNotConnected() throws IOException {
        transport.download();
    }

    @Test(expectedExceptions = IOException.class)
    public void uploadThrowsWhenNotConnected() throws IOException {
        transport.upload("some content");
    }

    @Test
    public void disconnectWithoutConnectDoesNotThrow() {
        // безопасный no-op: не должно бросать исключение, даже если connect() не вызывался
        transport.disconnect();

        Assert.assertFalse(transport.isConnected());
    }

    @Test
    public void disconnectCalledTwiceDoesNotThrow() {
        transport.disconnect();
        transport.disconnect();

        Assert.assertFalse(transport.isConnected());
    }

    @Test
    public void defaultConstructorUsesKnownHostsUnderUserHome() {
        String expectedPath = System.getProperty("user.home")
                + File.separator + ".ssh" + File.separator + "known_hosts";

        Assert.assertEquals(transport.getKnownHostsPath(), expectedPath);
    }

    @Test
    public void customKnownHostsPathIsUsed() {
        String customPath = "/custom/path/known_hosts";
        SftpFileTransport custom = new SftpFileTransport(
                "localhost", 22, "user", "password", "/remote/addresses.json", customPath);

        Assert.assertEquals(custom.getKnownHostsPath(), customPath);
    }

    // ---------- Проверка host key при подключении ----------

    @Test
    public void connectThrowsWhenKnownHostsFileMissing() {
        String missingPath = "/nonexistent-directory-for-test/known_hosts";
        SftpFileTransport withMissingKnownHosts = new SftpFileTransport(
                "localhost", 22, "user", "password", "/remote/addresses.json", missingPath);

        try {
            withMissingKnownHosts.connect();
            Assert.fail("Ожидался IOException из-за отсутствующего known_hosts файла");
        } catch (IOException e) {
            Assert.assertTrue(e.getMessage().contains("known_hosts"),
                    "Сообщение об ошибке должно упоминать known_hosts: " + e.getMessage());
        }
    }

    @Test
    public void connectDoesNotEstablishSessionWhenKnownHostsFileMissing() {
        String missingPath = "/nonexistent-directory-for-test/known_hosts";
        SftpFileTransport withMissingKnownHosts = new SftpFileTransport(
                "localhost", 22, "user", "password", "/remote/addresses.json", missingPath);

        try {
            withMissingKnownHosts.connect();
        } catch (IOException expected) {
            // ожидаемо - проверяем в отдельном тесте
        }

        Assert.assertFalse(withMissingKnownHosts.isConnected(),
                "Соединение не должно считаться установленным при ошибке known_hosts");
    }
}