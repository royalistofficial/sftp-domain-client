package com.infotecs.internship.ui;

import com.infotecs.internship.model.AddressEntry;
import com.infotecs.internship.storage.AddressBook;
import com.infotecs.internship.validation.IpV4Validator;
import com.infotecs.internship.validation.IpValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Тесты для {@link ConsoleMenu}.
 */
public class ConsoleMenuTest {

    private IpValidator ipValidator;
    private ByteArrayOutputStream outStream;

    @BeforeMethod
    public void setUp() {
        ipValidator = new IpV4Validator();
        outStream = new ByteArrayOutputStream();
    }

    private ConsoleMenu menuFor(AddressBook book, String input) {
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        PrintStream printStream = newPrintStream();
        return new ConsoleMenu(book, scanner, printStream);
    }

    private PrintStream newPrintStream() {
        try {
            return new PrintStream(outStream, true, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private String output() throws UnsupportedEncodingException {
        return outStream.toString(StandardCharsets.UTF_8.name());
    }

    @Test
    public void printsListAndExitsOnChoiceSix() throws UnsupportedEncodingException {
        AddressBook book = new AddressBook(
                java.util.Collections.singletonList(new AddressEntry("example.com", "192.168.0.1")),
                ipValidator);

        ConsoleMenu menu = menuFor(book, "1\n6\n");
        menu.run();

        String result = output();
        Assert.assertTrue(result.contains("example.com"));
        Assert.assertTrue(result.contains("192.168.0.1"));
        Assert.assertTrue(result.contains("Завершение работы"));
    }

    @Test
    public void invalidMenuChoiceShowsErrorAndDoesNotCrash() throws UnsupportedEncodingException {
        AddressBook book = new AddressBook(new ArrayList<>(), ipValidator);

        ConsoleMenu menu = menuFor(book, "9\n6\n");
        menu.run();

        String result = output();
        Assert.assertTrue(result.contains("Некорректный пункт меню"));
        Assert.assertTrue(result.contains("Завершение работы"));
    }

    @Test
    public void addEntryThroughMenuUpdatesAddressBook() throws UnsupportedEncodingException {
        AddressBook book = new AddressBook(new ArrayList<>(), ipValidator);

        ConsoleMenu menu = menuFor(book, "4\nexample.com\n192.168.0.1\n6\n");
        menu.run();

        Assert.assertEquals(book.findIpByDomain("example.com"), Optional.of("192.168.0.1"));
        Assert.assertTrue(output().contains("Запись добавлена"));
    }

    @Test
    public void removeEntryThroughMenuUpdatesAddressBook() throws UnsupportedEncodingException {
        AddressBook book = new AddressBook(
                java.util.Collections.singletonList(new AddressEntry("example.com", "192.168.0.1")),
                ipValidator);

        ConsoleMenu menu = menuFor(book, "5\nexample.com\n6\n");
        menu.run();

        Assert.assertFalse(book.findIpByDomain("example.com").isPresent());
        Assert.assertTrue(output().contains("Запись удалена"));
    }

    @Test
    public void findIpByDomainNotFoundShowsMessage() throws UnsupportedEncodingException {
        AddressBook book = new AddressBook(new ArrayList<>(), ipValidator);

        ConsoleMenu menu = menuFor(book, "2\nmissing.com\n6\n");
        menu.run();

        Assert.assertTrue(output().contains("Домен не найден"));
    }

    @Test
    public void addDuplicateShowsErrorAndDoesNotCrash() throws UnsupportedEncodingException {
        AddressBook book = new AddressBook(
                java.util.Collections.singletonList(new AddressEntry("example.com", "192.168.0.1")),
                ipValidator);

        ConsoleMenu menu = menuFor(book, "4\nexample.com\n9.9.9.9\n6\n");
        menu.run();

        String result = output();
        Assert.assertTrue(result.contains("Ошибка"));
        Assert.assertTrue(result.contains("Завершение работы"));
        Assert.assertEquals(book.getAllEntries().size(), 1, "Дубликат не должен быть добавлен");
    }

    @Test
    public void terminatesGracefullyWhenInputEndsUnexpectedly() throws UnsupportedEncodingException {
        AddressBook book = new AddressBook(new ArrayList<>(), ipValidator);

        // намеренно без "6" в конце - проверяем, что меню не зацикливается
        ConsoleMenu menu = menuFor(book, "1\n");
        menu.run();

        Assert.assertTrue(output().contains("Завершение работы"));
    }

    @Test
    public void onDataChangedCallbackInvokedAfterSuccessfulAdd() throws UnsupportedEncodingException {
        AddressBook book = new AddressBook(new ArrayList<>(), ipValidator);
        List<Boolean> callbackInvoked = new ArrayList<>();

        Scanner scanner = new Scanner(new ByteArrayInputStream(
                "4\nexample.com\n192.168.0.1\n6\n".getBytes(StandardCharsets.UTF_8)));
        ConsoleMenu menu = new ConsoleMenu(book, scanner, newPrintStream(), () -> callbackInvoked.add(true));

        menu.run();

        Assert.assertEquals(callbackInvoked.size(), 1, "onDataChanged должен вызываться ровно один раз");
    }

    @Test
    public void onDataChangedFailureShowsMessageWithoutCrashing() throws UnsupportedEncodingException {
        AddressBook book = new AddressBook(new ArrayList<>(), ipValidator);

        Scanner scanner = new Scanner(new ByteArrayInputStream(
                "4\nexample.com\n192.168.0.1\n6\n".getBytes(StandardCharsets.UTF_8)));
        Runnable failingCallback = () -> {
            throw new RuntimeException("simulated save failure");
        };
        ConsoleMenu menu = new ConsoleMenu(book, scanner, newPrintStream(), failingCallback);

        menu.run();

        String result = output();
        Assert.assertTrue(result.contains("Не удалось сохранить изменения"));
        Assert.assertTrue(result.contains("Завершение работы"));
    }
}