# Задание 1

Код решения: `com.github.sibmaks.itmodb.hw3.service.DataImportServiceImpl.processImport`

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

# Задание 2

Код решения: `com.github.sibmaks.itmodb.hw3.controller.EventCreatorController.create`

Для запуска создания событий поездок можно выполнить запрос


```shell
curl --location 'http://localhost:8080/event-controller/create' \
--header 'Content-Type: application/json' \
--data '{}'
```

К БД будет выполнен запрос:

```json
[
  {
    "$match": {
      "$expr": {
        "$gt": [
          "$tpepDropOffMillis",
          {
            "$add": [
              "$tpepPickupMillis",
              60000
            ]
          }
        ]
      }
    }
  },
  {
    "$facet": {
      "startTrips": [
        {
          "$project": {
            "tripId": "$_id",
            "eventType": {
              "$literal": "start"
            },
            "eventTime": "$tpepPickupMillis",
            "VendorID": 1,
            "tpepPickupDatetime": 1,
            "tpepPickupMillis": 1,
            "PULocationID": 1,
            "_id": 0
          }
        }
      ],
      "endTrips": [
        {
          "$project": {
            "tripId": "$_id",
            "eventType": {
              "$literal": "end"
            },
            "eventTime": "$tpepDropOffMillis",
            "tpepDropOffDatetime": 1,
            "tpepDropOffMillis": 1,
            "passengerCount": 1,
            "tripDistance": 1,
            "rateCodeID": 1,
            "storeAndFwdFlag": 1,
            "PULocationID": 1,
            "DOLocationID": 1,
            "paymentType": 1,
            "fareAmount": 1,
            "extra": 1,
            "mtaTax": 1,
            "tipAmount": 1,
            "tollsAmount": 1,
            "improvementSurcharge": 1,
            "totalAmount": 1,
            "_id": 0
          }
        }
      ]
    }
  },
  {
    "$project": {
      "allEvents": {
        "$concatArrays": [
          "$startTrips",
          "$endTrips"
        ]
      }
    }
  },
  {
    "$unwind": "$allEvents"
  },
  {
    "$replaceRoot": {
      "newRoot": "$allEvents"
    }
  },
  {
    "$out": "trip_events"
  }
]
```

И будет создана коллекция: `trip_events`.

# Задание 3

Код отправки в топик: `com.github.sibmaks.itmodb.hw3.controller.EventSenderController.send`

Создадим топик: `taxi_trip` со следующей конфигурацией

```properties
# Храним данные минимум неделю
retention.ms=7 days
# Чистка каждый день
delete.retention.ms=1 day
# Старые данные удаляем
cleanup.policy=delete
# Ограничиваем размер сообщений в 4 МБ
max.message.bytes=4MiB
```

Фактор репликации 1, так как работаем локально.

Количество партиций: 4, взято наугад, для более точной настройки желательно понимать контекст использования,

В целом 4 одновременных читателя кажется для не большого потока данных достаточным.

В качестве ключа будет использовать `tripId`, так как он является `UUID`-ом получим относительно равномерное распределение по партициям.

Конфигурация `producer-а`:
```properties
# ожидаем ответа от всех нод
kafka.producer.acks=all
# в случае ошибки выполняем 3 попытки
kafka.producer.retries=3
# задержка между повторами
kafka.producer.retry.backoff.ms=1000

# ключ и значение отправляем в виде строки
kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer

# шифрование не используем
kafka.producer.security.protocol=PLAINTEXT

# используем встроенную идемпотентность для kafka-producer-а
kafka.producer.enable.idempotence=true
```

# Задание 4 и 5

Код решения в `com.github.sibmaks.itmodb.hw3.listener.kafka.TripListener.onEvent`

Конфигурация подключения к Redis

```properties
spring.data.redis.client-name=sibmaks-local
spring.data.redis.client-type=lettuce
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

Конфигурация подключения к Kafka в качестве consumer-а
```properties
# Читаем только записанное
spring.kafka.consumer.isolation-level=read_committed

# Всегда читаем с самого 1-ого не прочитанного сообщения
spring.kafka.consumer.auto-offset-reset=earliest

# Авто-коммит не нужен, будем делать acknowledgment руками
spring.kafka.consumer.enable-auto-commit=false
spring.kafka.listener.ack-mode=manual

# Ключ-значение строки, как и в producer-е
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```