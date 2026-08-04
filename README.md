# SFTP Domain Client

Консольный клиент для работы с доменными адресами (пары "домен - IP"),
хранящимися в JSON-подобном файле на SFTP-сервере.

## Структура проекта

- `client` — модуль с консольным приложением.
- `tests` — модуль с автотестами (TestNG).

## Сборка

```
mvn clean package
```

После сборки:
- исполняемый jar клиента: `client/target/sftp-domain-client.jar`
- исполняемый jar тестов: `tests/target/sftp-domain-client-tests.jar`

## Запуск клиента

```
java -jar client/target/sftp-domain-client.jar <host> <port> <user> <password>
```

## Запуск тестов

```
mvn clean test -pl tests -am
```

или напрямую собранным jar-ом:

```
java -jar tests/target/sftp-domain-client-tests.jar
```
