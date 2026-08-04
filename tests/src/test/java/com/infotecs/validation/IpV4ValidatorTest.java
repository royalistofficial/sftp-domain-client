package com.infotecs.internship.validation;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Тесты для {@link IpV4Validator}.
 */
public class IpV4ValidatorTest {

    private IpValidator validator;

    @BeforeMethod
    public void setUp() {
        validator = new IpV4Validator();
    }

    @DataProvider(name = "validIps")
    public Object[][] validIps() {
        return new Object[][] {
                {"192.168.0.1"},
                {"0.0.0.0"},
                {"255.255.255.255"},
                {"1.2.3.4"},
                {"10.0.0.1"},
                {"127.0.0.1"},
        };
    }

    @Test(dataProvider = "validIps")
    public void validIpsAreAccepted(String ip) {
        Assert.assertTrue(validator.isValid(ip), "Ожидался валидный IP: " + ip);
    }

    @DataProvider(name = "invalidIps")
    public Object[][] invalidIps() {
        return new Object[][] {
                {"256.1.1.1"},
                {"1.256.1.1"},
                {"999.999.999.999"},
                {"1.1.1"},
                {"1.1"},
                {"1"},
                {"1.1.1.1.1"},
                {"a.b.c.d"},
                {"192.168.0.a"},
                {"01.1.1.1"},
                {"1.01.1.1"},
                {"192.168.00.1"},
                {""},
                {(String) null},
                {" 1.1.1.1"},
                {"1.1.1.1 "},
                {"1. 1.1.1"},
                {"1..1.1"},
                {".1.1.1"},
                {"1.1.1."},
                {"1.1.1.1."},
                {"-1.1.1.1"},
        };
    }

    @Test(dataProvider = "invalidIps")
    public void invalidIpsAreRejected(String ip) {
        Assert.assertFalse(validator.isValid(ip), "Ожидался невалидный IP: " + ip);
    }

    @Test
    public void zeroOctetWithoutLeadingZeroIsValid() {
        Assert.assertTrue(validator.isValid("0.0.0.0"));
    }

    @Test
    public void boundaryValueOctetIsValid() {
        Assert.assertTrue(validator.isValid("255.0.0.0"));
    }

    @Test
    public void boundaryValueOctetPlusOneIsInvalid() {
        Assert.assertFalse(validator.isValid("256.0.0.0"));
    }
}