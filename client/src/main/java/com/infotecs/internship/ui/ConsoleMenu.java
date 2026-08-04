package com.infotecs.internship.ui;

import com.infotecs.internship.model.AddressEntry;
import com.infotecs.internship.storage.AddressBook;
import com.infotecs.internship.storage.DuplicateEntryException;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Консольное меню для работы с {@link AddressBook}.
 */
public class ConsoleMenu {

    private static final String MENU_TEXT =
            "\n=== SFTP Domain Client ===\n"
                    + "1. Получить список пар \"домен - адрес\"\n"
                    + "2. Получить IP-адрес по доменному имени\n"
                    + "3. Получить доменное имя по IP-адресу\n"
                    + "4. Добавить новую пару \"домен - адрес\"\n"
                    + "5. Удалить пару \"домен - адрес\"\n"
                    + "6. Завершить работу\n"
                    + "Выберите пункт меню: ";

    private final AddressBook book;
    private final Scanner in;
    private final PrintStream out;
    private final Runnable onDataChanged;

    /**
     * Конструктор без сохранения изменений - предназначен для юнит-тестов
     * UI-слоя, где персистентность не проверяется.
     *
     * @param book хранилище пар "домен - IP"
     * @param in   источник пользовательского ввода
     * @param out  поток для вывода меню и результатов
     */
    public ConsoleMenu(AddressBook book, Scanner in, PrintStream out) {
        this(book, in, out, () -> { });
    }

    /**
     * @param book          хранилище пар "домен - IP"
     * @param in            источник пользовательского ввода
     * @param out           поток для вывода меню и результатов
     * @param onDataChanged вызывается после успешного добавления или
     *                      удаления записи (например, для сохранения
     *                      изменений обратно на SFTP-сервер)
     */
    public ConsoleMenu(AddressBook book, Scanner in, PrintStream out, Runnable onDataChanged) {
        this.book = book;
        this.in = in;
        this.out = out;
        this.onDataChanged = onDataChanged;
    }

    /** Запускает цикл меню до выбора пункта "Завершить работу" или конца ввода. */
    public void run() {
        boolean running = true;
        while (running) {
            out.print(MENU_TEXT);
            String choice = readLine();
            switch (choice) {
                case "1":
                    handleList();
                    break;
                case "2":
                    handleFindIpByDomain();
                    break;
                case "3":
                    handleFindDomainByIp();
                    break;
                case "4":
                    handleAdd();
                    break;
                case "5":
                    handleRemove();
                    break;
                case "6":
                    out.println("Завершение работы.");
                    running = false;
                    break;
                default:
                    out.println("Некорректный пункт меню: \"" + choice + "\". Попробуйте снова.");
                    break;
            }
        }
    }

    private void handleList() {
        List<AddressEntry> entries = book.listSortedByDomain();
        if (entries.isEmpty()) {
            out.println("Список пуст.");
            return;
        }
        for (AddressEntry entry : entries) {
            out.println(entry.getDomain() + " - " + entry.getIp());
        }
    }

    private void handleFindIpByDomain() {
        out.print("Введите домен: ");
        String domain = readLine();
        Optional<String> ip = book.findIpByDomain(domain);
        if (ip.isPresent()) {
            out.println("IP-адрес: " + ip.get());
        } else {
            out.println("Домен не найден.");
        }
    }

    private void handleFindDomainByIp() {
        out.print("Введите IP-адрес: ");
        String ip = readLine();
        Optional<String> domain = book.findDomainByIp(ip);
        if (domain.isPresent()) {
            out.println("Домен: " + domain.get());
        } else {
            out.println("IP-адрес не найден.");
        }
    }

    private void handleAdd() {
        out.print("Введите домен: ");
        String domain = readLine();
        out.print("Введите IP-адрес: ");
        String ip = readLine();
        try {
            book.add(domain, ip);
            out.println("Запись добавлена.");
            notifyDataChanged();
        } catch (IllegalArgumentException | DuplicateEntryException e) {
            out.println("Ошибка: " + e.getMessage());
        }
    }

    private void handleRemove() {
        out.print("Введите домен или IP-адрес: ");
        String key = readLine();
        boolean removed = book.remove(key);
        if (removed) {
            out.println("Запись удалена.");
            notifyDataChanged();
        } else {
            out.println("Запись не найдена.");
        }
    }

    private void notifyDataChanged() {
        try {
            onDataChanged.run();
        } catch (RuntimeException e) {
            out.println("Не удалось сохранить изменения на сервере: " + e.getMessage());
        }
    }

    private String readLine() {
        if (in.hasNextLine()) {
            return in.nextLine().trim();
        }
        // ввод закончился - завершаем работу, чтобы не зациклиться
        return "6";
    }
}