package com.infotecs.internship.json;

import com.infotecs.internship.model.AddressEntry;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Тесты для {@link AddressJsonWriter}, включая проверки согласованности
 * с {@link AddressJsonParser} (round-trip: write -> parse).
 */
public class AddressJsonWriterTest {

    private AddressJsonWriter writer;
    private AddressJsonParser parser;

    @BeforeMethod
    public void setUp() {
        writer = new AddressJsonWriter();
        parser = new AddressJsonParser();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void writeThrowsOnNullList() {
        writer.write(null);
    }

    @Test
    public void writeEmptyListProducesParsableEmptyResult() {
        String json = writer.write(Collections.emptyList());

        List<AddressEntry> parsed = parser.parse(json);

        Assert.assertTrue(parsed.isEmpty());
    }

    @Test
    public void writeContainsAddressesKey() {
        String json = writer.write(Collections.emptyList());

        Assert.assertTrue(json.contains("\"addresses\""));
    }

    @Test
    public void roundTripPreservesSingleEntry() {
        List<AddressEntry> original = Collections.singletonList(
                new AddressEntry("example.com", "192.168.0.1"));

        String json = writer.write(original);
        List<AddressEntry> parsed = parser.parse(json);

        Assert.assertEquals(parsed, original);
    }

    @Test
    public void roundTripPreservesMultipleEntriesAndOrder() {
        List<AddressEntry> original = Arrays.asList(
                new AddressEntry("first.domain", "192.168.0.1"),
                new AddressEntry("second.domain", "192.168.0.2"),
                new AddressEntry("third.domain", "192.168.0.3"));

        String json = writer.write(original);
        List<AddressEntry> parsed = parser.parse(json);

        Assert.assertEquals(parsed, original);
    }

    @Test
    public void roundTripIsStableOnSecondPass() {
        List<AddressEntry> original = Arrays.asList(
                new AddressEntry("a.com", "1.1.1.1"),
                new AddressEntry("b.com", "2.2.2.2"));

        String firstJson = writer.write(original);
        List<AddressEntry> firstParsed = parser.parse(firstJson);
        String secondJson = writer.write(firstParsed);
        List<AddressEntry> secondParsed = parser.parse(secondJson);

        Assert.assertEquals(secondParsed, original);
    }

    @Test
    public void escapesQuotesAndBackslashesInValues() {
        List<AddressEntry> original = new ArrayList<>();
        original.add(new AddressEntry("weird\"domain\\name", "1.2.3.4"));

        String json = writer.write(original);
        List<AddressEntry> parsed = parser.parse(json);

        Assert.assertEquals(parsed, original);
    }
}