# Задание 1

Для проверки выполнения задания необходимо

## Настройка подключения к MongoDB
Необходимо в `application.properties`, либо аналогичным способом настроить следующие поля:

```properties
# подключение к MongoDB
spring.data.mongodb.uri=mongodb://login:pass@localhost:27017/itmo-db
# база данных в MongoDB
spring.data.mongodb.database=itmo-db

# Путь до файла для импорта
app.storage.root-path=/path-with-files/
```

## Запустить приложение

Можно через консоль:

```shell
./gradlew clean build bootRun
```

Проверить, что приложение успешно запущено

```shell
2024-01-09T22:52:03.761+03:00  INFO 20811 --- [  restartedMain] o.s.b.d.a.OptionalLiveReloadServer       : LiveReload server is running on port 35729
2024-01-09T22:52:04.124+03:00  INFO 20811 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path ''
2024-01-09T22:52:04.129+03:00  INFO 20811 --- [  restartedMain] c.g.sibmaks.itmodb.hw3.Hw3Application    : Started Hw3Application in 1.18 seconds (process running for 1.504)
```

## Запустить импорт

Посредством http запроса:

```shell
curl --location 'http://localhost:8080/import-controller/start' \
--header 'Content-Type: application/json' \
--data '{
"path": "2018_Yellow_Taxi_Trip_Data_20231108.csv"
}'
```

## Получение статуса импорта


Посредством http запроса:

```shell
curl --location 'http://localhost:8080/import-controller/state' \
--header 'Content-Type: application/json' \
--data '{
    "taskId": "f993940e-3bcc-46a1-9759-e4d4b05d8982"
}'
```