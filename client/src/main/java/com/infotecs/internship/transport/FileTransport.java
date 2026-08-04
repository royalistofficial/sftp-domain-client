package com.infotecs.internship.transport;

import java.io.IOException;

public interface FileTransport {

    /**
     * @return текущее содержимое файла
     * @throws IOException если файл не удалось прочитать
     */
    String download() throws IOException;

    /**
     * Полностью заменяет содержимое файла переданным значением.
     *
     * @param content новое содержимое файла
     * @throws IOException если файл не удалось записать
     */
    void upload(String content) throws IOException;
}