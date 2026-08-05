# SFTP Domain Client

Консольный клиент для работы с доменными адресами (пары "домен - IP"),
хранящимися в JSON-подобном файле на SFTP-сервере. Тестовое задание для
стажёра по направлению "Автотестирование (Java)".

## Возможности

После подключения к SFTP-серверу клиент выводит меню:

1. Получить список пар "домен - адрес" (отсортирован по алфавиту домена).
2. Получить IP-адрес по доменному имени.
3. Получить доменное имя по IP-адресу.
4. Добавить новую пару "домен - адрес" (с валидацией уникальности и формата IPv4).
5. Удалить пару "домен - адрес" по доменному имени или IP-адресу.
6. Завершить работу.

Изменения (добавление/удаление) сразу сохраняются обратно в файл на SFTP-сервере.

## Требования

- Java SE 8
- Maven 3.6+
- Из внешних библиотек используются только [JSch](http://www.jcraft.com/jsch/) (SFTP) и [TestNG](https://testng.org/) (тесты) - согласно условиям задания.

## Структура проекта

Multi-module Maven-проект:

```
sftp-domain-client/
├── client/   - модуль консольного приложения
└── tests/    - модуль автотестов (TestNG)
```

### `client` — исходный код приложения

```
com.infotecs.internship/
├── Main.java                    - точка входа, связывает все слои
│
├── cli/
│   ├── ArgsParser.java           - разбор аргументов командной строки
│   └── ConnectionParams.java     - параметры подключения (host, port, user, password, remoteFilePath)
│
├── model/
│   └── AddressEntry.java         - неизменяемая пара "домен - IP"
│
├── validation/
│   ├── IpValidator.java          - контракт валидации IP
│   └── IpV4Validator.java        - валидация формата IPv4
│
├── json/
│   ├── JsonValueParser.java      - универсальный JSON-парсер (без внешних библиотек)
│   ├── AddressJsonParser.java    - разбор файла адресов в List<AddressEntry>
│   ├── AddressJsonWriter.java    - сериализация List<AddressEntry> обратно в JSON
│   └── JsonParseException.java   - ошибки структуры/синтаксиса JSON
│
├── transport/
│   ├── FileTransport.java        - контракт "скачать/загрузить содержимое файла"
│   └── SftpFileTransport.java    - реализация на JSch
│
├── storage/
│   ├── AddressBook.java          - бизнес-логика: уникальность, поиск, добавление, удаление
│   ├── AddressFileService.java   - оркестратор: FileTransport + JSON + AddressBook
│   └── DuplicateEntryException.java
│
└── ui/
    └── ConsoleMenu.java           - консольное меню (Scanner/PrintStream через конструктор)
```

### `tests` — автотесты (TestNG)

Структура тестов зеркалит структуру `client`, чтобы было понятно, какой тест что проверяет:

```
com.infotecs.internship/
├── model/validation/json/transport/storage/ui/cli/...Test.java
└── tests/SmokeTest.java   - проверка, что модуль тестов собирается и запускается
```

## Сборка

```
mvn clean package
```

После сборки:

- исполняемый jar клиента: `client/target/sftp-domain-client.jar`
- исполняемый jar тестов: `tests/target/sftp-domain-client-tests.jar`

## Запуск клиента

```
java -jar client/target/sftp-domain-client.jar <host> <port> <username> <password> [remoteFilePath]
```

- `remoteFilePath` — необязательный параметр, путь к файлу с адресами на сервере. По умолчанию: `addresses.json`.

Пример:

```
java -jar client/target/sftp-domain-client.jar 127.0.0.1 22 testuser testpass /home/testuser/addresses.json
```

## Запуск тестов

```
mvn clean test 
```

или напрямую собранным jar-ом:

```
java -jar tests/target/sftp-domain-client-tests.jar
```

## Генерация Javadoc

```
mvn javadoc:javadoc
```

Результат: `target/site/apidocs/index.html`.