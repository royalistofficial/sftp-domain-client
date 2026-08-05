package com.infotecs.internship.storage;

import com.infotecs.internship.json.AddressJsonParser;
import com.infotecs.internship.json.AddressJsonWriter;
import com.infotecs.internship.model.AddressEntry;
import com.infotecs.internship.transport.FileTransport;
import com.infotecs.internship.validation.IpValidator;

import java.io.IOException;
import java.util.List;

/**
 * Связывает {@link FileTransport}, JSON-парсер/писатель и {@link AddressBook}
 * в единый цикл "загрузить файл -> получить AddressBook" /
 * "сохранить текущее состояние AddressBook -> записать файл".
 */
public class AddressFileService {

    private final FileTransport transport;
    private final AddressJsonParser parser = new AddressJsonParser();
    private final AddressJsonWriter writer = new AddressJsonWriter();

    /** @param transport транспорт для чтения/записи файла с адресами */
    public AddressFileService(FileTransport transport) {
        this.transport = transport;
    }

    /**
     * Загружает файл через транспорт и строит {@link AddressBook} на его основе.
     *
     * @param ipValidator валидатор, который будет использоваться внутри
     *                    {@link AddressBook} при последующих добавлениях
     * @return хранилище, заполненное данными из файла
     * @throws IOException если файл не удалось скачать
     */
    public AddressBook load(IpValidator ipValidator) throws IOException {
        String json = transport.download();
        List<AddressEntry> entries = parser.parse(json);
        return new AddressBook(entries, ipValidator);
    }

    /**
     * Сериализует текущее состояние {@link AddressBook} и сохраняет его
     * через транспорт, перезаписывая файл на сервере.
     *
     * @param book хранилище, чьё состояние нужно сохранить
     * @throws IOException если файл не удалось загрузить на сервер
     */
    public void save(AddressBook book) throws IOException {
        String json = writer.write(book.getAllEntries());
        transport.upload(json);
    }
}