package com.infotecs.internship.storage;

import com.infotecs.internship.model.AddressEntry;
import com.infotecs.internship.validation.IpV4Validator;
import com.infotecs.internship.validation.IpValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Тесты для {@link AddressBook}: основной блок позитивных и негативных
 * сценариев бизнес-логики хранения пар "домен - IP".
 */
public class AddressBookTest {

    private IpValidator ipValidator;

    @BeforeMethod
    public void setUp() {
        ipValidator = new IpV4Validator();
    }

    private AddressBook emptyBook() {
        return new AddressBook(new ArrayList<>(), ipValidator);
    }

    private AddressBook bookWith(AddressEntry... entries) {
        return new AddressBook(Arrays.asList(entries), ipValidator);
    }

    // ---------- Позитивные сценарии ----------

    @Test
    public void listSortedByDomainReturnsAlphabeticalOrder() {
        AddressBook book = bookWith(
                new AddressEntry("zebra.com", "1.1.1.1"),
                new AddressEntry("apple.com", "2.2.2.2"),
                new AddressEntry("mango.com", "3.3.3.3"));

        List<AddressEntry> result = book.listSortedByDomain();

        Assert.assertEquals(result.get(0).getDomain(), "apple.com");
        Assert.assertEquals(result.get(1).getDomain(), "mango.com");
        Assert.assertEquals(result.get(2).getDomain(), "zebra.com");
    }

    @Test
    public void findIpByDomainReturnsIpForExistingDomain() {
        AddressBook book = bookWith(new AddressEntry("example.com", "192.168.0.1"));

        Optional<String> result = book.findIpByDomain("example.com");

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get(), "192.168.0.1");
    }

    @Test
    public void findDomainByIpReturnsDomainForExistingIp() {
        AddressBook book = bookWith(new AddressEntry("example.com", "192.168.0.1"));

        Optional<String> result = book.findDomainByIp("192.168.0.1");

        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get(), "example.com");
    }

    @Test
    public void addSuccessfullyAddsUniqueEntry() {
        AddressBook book = emptyBook();

        book.add("example.com", "192.168.0.1");

        Assert.assertEquals(book.findIpByDomain("example.com"), Optional.of("192.168.0.1"));
        Assert.assertEquals(book.getAllEntries().size(), 1);
    }

    @Test
    public void removeByDomainDeletesEntry() {
        AddressBook book = bookWith(new AddressEntry("example.com", "192.168.0.1"));

        boolean removed = book.remove("example.com");

        Assert.assertTrue(removed);
        Assert.assertFalse(book.findIpByDomain("example.com").isPresent());
        Assert.assertFalse(book.findDomainByIp("192.168.0.1").isPresent());
    }

    @Test
    public void removeByIpDeletesEntry() {
        AddressBook book = bookWith(new AddressEntry("example.com", "192.168.0.1"));

        boolean removed = book.remove("192.168.0.1");

        Assert.assertTrue(removed);
        Assert.assertFalse(book.findIpByDomain("example.com").isPresent());
        Assert.assertFalse(book.findDomainByIp("192.168.0.1").isPresent());
    }

    // ---------- Негативные сценарии ----------

    @Test
    public void findIpByDomainReturnsEmptyForUnknownDomain() {
        AddressBook book = emptyBook();

        Assert.assertFalse(book.findIpByDomain("unknown.com").isPresent());
    }

    @Test
    public void findDomainByIpReturnsEmptyForUnknownIp() {
        AddressBook book = emptyBook();

        Assert.assertFalse(book.findDomainByIp("9.9.9.9").isPresent());
    }

    @Test(expectedExceptions = DuplicateEntryException.class)
    public void addThrowsOnDuplicateDomain() {
        AddressBook book = bookWith(new AddressEntry("example.com", "192.168.0.1"));

        book.add("example.com", "192.168.0.2");
    }

    @Test(expectedExceptions = DuplicateEntryException.class)
    public void addThrowsOnDuplicateIp() {
        AddressBook book = bookWith(new AddressEntry("example.com", "192.168.0.1"));

        book.add("other.com", "192.168.0.1");
    }

    @Test
    public void addWithDuplicateDomainDoesNotChangeState() {
        AddressBook book = bookWith(new AddressEntry("example.com", "192.168.0.1"));

        try {
            book.add("example.com", "192.168.0.2");
            Assert.fail("Expected DuplicateEntryException");
        } catch (DuplicateEntryException expected) {
            // ожидаемо
        }

        Assert.assertEquals(book.getAllEntries().size(), 1);
        Assert.assertEquals(book.findIpByDomain("example.com"), Optional.of("192.168.0.1"));
    }

    @DataProvider(name = "invalidIpsForAdd")
    public Object[][] invalidIpsForAdd() {
        return new Object[][] {
                {"256.1.1.1"},
                {"1.1.1"},
                {"a.b.c.d"},
                {"01.1.1.1"},
        };
    }

    @Test(dataProvider = "invalidIpsForAdd", expectedExceptions = IllegalArgumentException.class)
    public void addThrowsOnInvalidIpFormat(String invalidIp) {
        AddressBook book = emptyBook();

        book.add("example.com", invalidIp);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void addThrowsOnEmptyDomain() {
        AddressBook book = emptyBook();

        book.add("", "192.168.0.1");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void addThrowsOnNullDomain() {
        AddressBook book = emptyBook();

        book.add(null, "192.168.0.1");
    }

    @Test
    public void removeReturnsFalseForUnknownDomainOrIp() {
        AddressBook book = bookWith(new AddressEntry("example.com", "192.168.0.1"));

        boolean removed = book.remove("unknown.com");

        Assert.assertFalse(removed);
        Assert.assertEquals(book.getAllEntries().size(), 1);
    }

    @Test
    public void removeDoesNotThrowOnEmptyBook() {
        AddressBook book = emptyBook();

        boolean removed = book.remove("anything");

        Assert.assertFalse(removed);
    }

    @Test(expectedExceptions = DuplicateEntryException.class)
    public void constructorThrowsOnDuplicateDomainInInitialList() {
        new AddressBook(Arrays.asList(
                new AddressEntry("example.com", "1.1.1.1"),
                new AddressEntry("example.com", "2.2.2.2")
        ), ipValidator);
    }

    @Test(expectedExceptions = DuplicateEntryException.class)
    public void constructorThrowsOnDuplicateIpInInitialList() {
        new AddressBook(Arrays.asList(
                new AddressEntry("first.com", "1.1.1.1"),
                new AddressEntry("second.com", "1.1.1.1")
        ), ipValidator);
    }

    @Test
    public void getAllEntriesReturnsIndependentCopy() {
        AddressBook book = bookWith(new AddressEntry("example.com", "192.168.0.1"));

        List<AddressEntry> entries = book.getAllEntries();
        entries.clear(); // мутируем возвращённый список

        Assert.assertEquals(book.getAllEntries().size(), 1, "Внутреннее состояние не должно меняться");
    }

    @Test
    public void emptyBookReturnsEmptyList() {
        AddressBook book = emptyBook();

        Assert.assertTrue(book.getAllEntries().isEmpty());
        Assert.assertTrue(book.listSortedByDomain().isEmpty());
    }

    @Test
    public void constructorAcceptsEmptyInitialList() {
        AddressBook book = new AddressBook(Collections.emptyList(), ipValidator);

        Assert.assertTrue(book.getAllEntries().isEmpty());
    }
}