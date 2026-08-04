package com.infotecs.internship.model;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Тесты для {@link AddressEntry}.
 */
public class AddressEntryTest {

    @Test
    public void constructorSetsDomainAndIp() {
        AddressEntry entry = new AddressEntry("example.com", "192.168.0.1");

        Assert.assertEquals(entry.getDomain(), "example.com");
        Assert.assertEquals(entry.getIp(), "192.168.0.1");
    }

    @Test
    public void equalsReturnsTrueForSameDomainAndIp() {
        AddressEntry first = new AddressEntry("example.com", "192.168.0.1");
        AddressEntry second = new AddressEntry("example.com", "192.168.0.1");

        Assert.assertEquals(first, second);
    }

    @Test
    public void hashCodeIsSameForEqualObjects() {
        AddressEntry first = new AddressEntry("example.com", "192.168.0.1");
        AddressEntry second = new AddressEntry("example.com", "192.168.0.1");

        Assert.assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    public void equalsReturnsFalseForDifferentDomain() {
        AddressEntry first = new AddressEntry("example.com", "192.168.0.1");
        AddressEntry second = new AddressEntry("other.com", "192.168.0.1");

        Assert.assertNotEquals(first, second);
    }

    @Test
    public void equalsReturnsFalseForDifferentIp() {
        AddressEntry first = new AddressEntry("example.com", "192.168.0.1");
        AddressEntry second = new AddressEntry("example.com", "192.168.0.2");

        Assert.assertNotEquals(first, second);
    }

    @Test
    public void equalsReturnsFalseForNull() {
        AddressEntry entry = new AddressEntry("example.com", "192.168.0.1");

        Assert.assertNotEquals(entry, null);
    }

    @Test
    public void equalsReturnsFalseForDifferentType() {
        AddressEntry entry = new AddressEntry("example.com", "192.168.0.1");

        Assert.assertNotEquals(entry, "example.com");
    }

    @Test
    public void equalsIsReflexive() {
        AddressEntry entry = new AddressEntry("example.com", "192.168.0.1");

        Assert.assertEquals(entry, entry);
    }

    @Test
    public void toStringContainsDomainAndIp() {
        AddressEntry entry = new AddressEntry("example.com", "192.168.0.1");

        String result = entry.toString();

        Assert.assertTrue(result.contains("example.com"));
        Assert.assertTrue(result.contains("192.168.0.1"));
    }

    @DataProvider(name = "invalidDomain")
    public Object[][] invalidDomainProvider() {
        return new Object[][] {
                {null},
                {""},
                {"   "}
        };
    }

    @Test(dataProvider = "invalidDomain", expectedExceptions = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidDomain(String domain) {
        new AddressEntry(domain, "192.168.0.1");
    }

    @DataProvider(name = "invalidIp")
    public Object[][] invalidIpProvider() {
        return new Object[][] {
                {null},
                {""},
                {"   "}
        };
    }

    @Test(dataProvider = "invalidIp", expectedExceptions = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidIp(String ip) {
        new AddressEntry("example.com", ip);
    }
}