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
- Из внешних библиотек используются только [JSch](http://www.jcraft.com/jsch/) (SFTP) и [TestNG](https://testng.org/) (тесты).

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

Клиент проверяет отпечаток сервера по файлу `~/.ssh/known_hosts`
(`StrictHostKeyChecking=yes`). Если сервер ещё не был добавлен в
`known_hosts`, подключение будет отклонено с сообщением об
ошибке.

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

## Локальный SFTP-сервер для тестирования

Для ручной проверки клиента отдельный SFTP-демон ставить не нужно —
SFTP работает поверх обычного SSH, встроенного в Linux (OpenSSH).

### 1. Установить и запустить OpenSSH-сервер

```bash
sudo apt update
sudo apt install openssh-server -y
sudo systemctl enable --now ssh
sudo systemctl status ssh
```

### 2. Создать тестового пользователя и стартовый файл адресов

```bash
sudo useradd -m testuser
sudo passwd testuser
```


### 3. Убедиться, что вход по паролю разрешён

```bash
sudo grep -i passwordauthentication /etc/ssh/sshd_config /etc/ssh/sshd_config.d/*.conf 2>/dev/null
```

Если найдёте активную (не закомментированную) строку
`PasswordAuthentication no` — измените на `yes` и перезапустите сервис:

```bash
sudo systemctl restart ssh
```

### 4. Добавить отпечаток сервера в known_hosts

Клиент строго проверяет host key (`StrictHostKeyChecking=yes`), поэтому
перед первым запуском нужно явно доверить серверу. Проще всего —
один раз подключиться обычным `ssh` и подтвердить отпечаток:

```bash
ssh testuser@127.0.0.1
```

```
The authenticity of host '127.0.0.1 (127.0.0.1)' can't be established.
ED25519 key fingerprint is SHA256:xxxxxxxx...
Are you sure you want to continue connecting (yes/no/[fingerprint])? yes
```

Введите `yes`, авторизуйтесь паролем, затем `exit`. Отпечаток
запишется в `~/.ssh/known_hosts`, и Java-клиент сможет подключаться
без ошибок.

Альтернатива без интерактивного ввода:

```bash
ssh-keyscan -p 22 127.0.0.1 >> ~/.ssh/known_hosts
```

Для порядка можно сверить добавленный отпечаток с реальным ключом
сервера:

```bash
ssh-keygen -lf ~/.ssh/known_hosts -F 127.0.0.1
for key in /etc/ssh/ssh_host_*_key.pub; do sudo ssh-keygen -lf "$key"; done
```
Один из отпечатков во втором выводе должен совпасть с первым.

### 5. Проверить связку логин/пароль до запуска Java-клиента

```bash
sftp -P 22 testuser@127.0.0.1
```

Если подключение проходит с первого раза — переходите к запуску
клиента. Если просит пароль повторно ("Permission denied") — пароль
неверный, повторите шаг 2 (`sudo passwd testuser`).

### 6. Запустить клиент

```bash
java -jar client/target/sftp-domain-client.jar 127.0.0.1 22 testuser <ваш_пароль> /home/testuser/addresses.json
```
