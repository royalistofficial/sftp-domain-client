package com.infotecs.internship.transport;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
}