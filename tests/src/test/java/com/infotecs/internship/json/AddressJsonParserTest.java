package com.infotecs.internship.json;

import com.infotecs.internship.model.AddressEntry;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Тесты для {@link AddressJsonParser}.
 */
public class AddressJsonParserTest {

    private AddressJsonParser parser;

    @BeforeMethod
    public void setUp() {
        parser = new AddressJsonParser();
    }

    @Test
    public void parsesExampleFromTaskDescription() {
        String json = "{\n"
                + "  \"addresses\": [\n"
                + "    {\"domain\": \"first.domain\", \"ip\": \"192.168.0.1\"},\n"
                + "    {\"domain\": \"second.domain\", \"ip\": \"192.168.0.2\"},\n"
                + "    {\"domain\": \"third.domain\", \"ip\": \"192.168.0.3\"}\n"
                + "  ]\n"
                + "}";

        List<AddressEntry> result = parser.parse(json);

        Assert.assertEquals(result.size(), 3);
        Assert.assertEquals(result.get(0), new AddressEntry("first.domain", "192.168.0.1"));
        Assert.assertEquals(result.get(1), new AddressEntry("second.domain", "192.168.0.2"));
        Assert.assertEquals(result.get(2), new AddressEntry("third.domain", "192.168.0.3"));
    }

    @Test
    public void parsesEntriesInOriginalOrder() {
        String json = "{\"addresses\": ["
                + "{\"domain\": \"z.com\", \"ip\": \"1.1.1.1\"},"
                + "{\"domain\": \"a.com\", \"ip\": \"2.2.2.2\"}"
                + "]}";

        List<AddressEntry> result = parser.parse(json);

        // парсер не сортирует - порядок вывода по алфавиту это задача storage-слоя
        Assert.assertEquals(result.get(0).getDomain(), "z.com");
        Assert.assertEquals(result.get(1).getDomain(), "a.com");
    }

    @Test
    public void emptyAddressesArrayProducesEmptyList() {
        String json = "{\"addresses\": []}";

        List<AddressEntry> result = parser.parse(json);

        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void ignoresWhitespaceAndFormatting() {
        String json = "{ \"addresses\" : [ { \"domain\" : \"example.com\" , \"ip\" : \"1.2.3.4\" } ] }";

        List<AddressEntry> result = parser.parse(json);

        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0), new AddressEntry("example.com", "1.2.3.4"));
    }

    @Test(expectedExceptions = JsonParseException.class)
    public void nullJsonThrowsException() {
        parser.parse(null);
    }

    @DataProvider(name = "structurallyInvalidJson")
    public Object[][] structurallyInvalidJson() {
        return new Object[][] {
                {"{not valid json"},
                {"{\"addresses\": [}"},
                {""},
                {"[]"},
                {"\"just a string\""},
                {"{}"},
                {"{\"other\": []}"},
                {"{\"addresses\": \"not an array\"}"},
                {"{\"addresses\": {}}"},
                {"{\"addresses\": [\"not an object\"]}"},
                {"{\"addresses\": [123]}"},
                {"{\"addresses\": [{\"ip\": \"1.2.3.4\"}]}"},
                {"{\"addresses\": [{\"domain\": \"example.com\"}]}"},
                {"{\"addresses\": [{\"domain\": 123, \"ip\": \"1.2.3.4\"}]}"},
                {"{\"addresses\": [{\"domain\": \"example.com\", \"ip\": null}]}"},
                {"{\"addresses\": [{\"domain\": \"\", \"ip\": \"1.2.3.4\"}]}"},
        };
    }

    @Test(dataProvider = "structurallyInvalidJson", expectedExceptions = JsonParseException.class)
    public void invalidJsonThrowsJsonParseException(String json) {
        parser.parse(json);
    }

    @Test
    public void errorMessageContainsIndexOfInvalidEntry() {
        String json = "{\"addresses\": ["
                + "{\"domain\": \"first.com\", \"ip\": \"1.1.1.1\"},"
                + "{\"domain\": \"second.com\"}"
                + "]}";

        try {
            parser.parse(json);
            Assert.fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            Assert.assertTrue(e.getMessage().contains("index 1"),
                    "Сообщение об ошибке должно указывать на индекс некорректной записи: " + e.getMessage());
        }
    }
}