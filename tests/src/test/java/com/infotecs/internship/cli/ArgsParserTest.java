package com.infotecs.internship.cli;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Тесты для {@link ArgsParser}.
 */
public class ArgsParserTest {

    private ArgsParser parser;

    @BeforeMethod
    public void setUp() {
        parser = new ArgsParser();
    }

    @Test
    public void parsesValidArgsWithoutOptionalPath() {
        ConnectionParams params = parser.parse(new String[] {"sftp.example.com", "22", "user", "pass"});

        Assert.assertEquals(params.getHost(), "sftp.example.com");
        Assert.assertEquals(params.getPort(), 22);
        Assert.assertEquals(params.getUsername(), "user");
        Assert.assertEquals(params.getPassword(), "pass");
        Assert.assertEquals(params.getRemoteFilePath(), "addresses.json");
    }

    @Test
    public void parsesValidArgsWithOptionalPath() {
        ConnectionParams params = parser.parse(
                new String[] {"sftp.example.com", "2222", "user", "pass", "data/addresses.json"});

        Assert.assertEquals(params.getPort(), 2222);
        Assert.assertEquals(params.getRemoteFilePath(), "data/addresses.json");
    }

    @Test
    public void boundaryPortValuesAreAccepted() {
        Assert.assertEquals(parser.parse(new String[] {"h", "1", "u", "p"}).getPort(), 1);
        Assert.assertEquals(parser.parse(new String[] {"h", "65535", "u", "p"}).getPort(), 65535);
    }

    @DataProvider(name = "invalidArgCounts")
    public Object[][] invalidArgCounts() {
        return new Object[][] {
                {new Object[] {}},
                {new Object[] {"host"}},
                {new Object[] {"host", "22"}},
                {new Object[] {"host", "22", "user"}},
                {new Object[] {"host", "22", "user", "pass", "path", "extra"}},
        };
    }

    @Test(dataProvider = "invalidArgCounts", expectedExceptions = IllegalArgumentException.class)
    public void throwsOnWrongNumberOfArgs(Object[] args) {
        parser.parse(toStringArray(args));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void throwsOnNullArgs() {
        parser.parse(null);
    }

    @DataProvider(name = "invalidPorts")
    public Object[][] invalidPorts() {
        return new Object[][] {
                {"not-a-number"},
                {"0"},
                {"-1"},
                {"65536"},
                {"999999"},
                {""},
        };
    }

    @Test(dataProvider = "invalidPorts", expectedExceptions = IllegalArgumentException.class)
    public void throwsOnInvalidPort(String port) {
        parser.parse(new String[] {"host", port, "user", "pass"});
    }

    private String[] toStringArray(Object[] args) {
        String[] result = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = (String) args[i];
        }
        return result;
    }
}