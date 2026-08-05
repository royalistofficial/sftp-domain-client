package com.infotecs.internship;

import com.infotecs.internship.cli.ArgsParser;
import com.infotecs.internship.cli.ConnectionParams;
import com.infotecs.internship.json.JsonParseException;
import com.infotecs.internship.storage.AddressBook;
import com.infotecs.internship.storage.AddressFileService;
import com.infotecs.internship.storage.DuplicateEntryException;
import com.infotecs.internship.transport.SftpFileTransport;
import com.infotecs.internship.ui.ConsoleMenu;
import com.infotecs.internship.validation.IpV4Validator;
import com.infotecs.internship.validation.IpValidator;

import java.io.IOException;
import java.util.Scanner;

/**
 * Точка входа консольного SFTP-клиента.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        ConnectionParams params;
        try {
            params = new ArgsParser().parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        SftpFileTransport transport = new SftpFileTransport(
                params.getHost(), params.getPort(), params.getUsername(),
                params.getPassword(), params.getRemoteFilePath());

        try {
            runApplication(transport, params);
        } finally {
            transport.disconnect();
        }
    }

    private static void runApplication(SftpFileTransport transport, ConnectionParams params) {
        System.out.println("Подключение к " + params.getHost() + ":" + params.getPort() + "...");
        try {
            transport.connect();
        } catch (IOException e) {
            System.err.println("Не удалось подключиться к SFTP-серверу: " + e.getMessage());
            System.exit(1);
            return;
        }
        System.out.println("Подключение установлено.");

        AddressFileService fileService = new AddressFileService(transport);
        IpValidator ipValidator = new IpV4Validator();

        AddressBook book;
        try {
            book = fileService.load(ipValidator);
        } catch (IOException e) {
            System.err.println("Не удалось загрузить файл " + params.getRemoteFilePath() + ": " + e.getMessage());
            System.exit(1);
            return;
        } catch (JsonParseException | DuplicateEntryException e) {
            System.err.println("Файл " + params.getRemoteFilePath() + " имеет некорректный формат: " + e.getMessage());
            System.exit(1);
            return;
        }

        Runnable saveToServer = () -> {
            try {
                fileService.save(book);
            } catch (IOException e) {
                throw new RuntimeException("Ошибка сохранения на сервер: " + e.getMessage(), e);
            }
        };

        ConsoleMenu menu = new ConsoleMenu(book, new Scanner(System.in), System.out, saveToServer);
        menu.run();
    }
}