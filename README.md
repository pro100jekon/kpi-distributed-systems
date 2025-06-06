### Налаштування кластера Kafka
#### Створимо тестовий топік на машині №2

![img.png](.img/lab4/img.png)

#### Перевіримо що топік був створений на машині №1

![img_1.png](.img/lab4/img_1.png)

#### Подивимось на топік з машини №3

![img_2.png](.img/lab4/img_2.png)

#### На машинах №1 та №2 запустимо `consumer` та `producer`

![img_3.png](.img/lab4/img_3.png)
![img_4.png](.img/lab4/img_4.png)
![img_5.png](.img/lab4/img_5.png)
![img_6.png](.img/lab4/img_6.png)

#### Реплікація даних працює

## Базова функціональність системи
Проєкт складається з трьох модулів: `messages-service`, `logging-service` та `facade-service`. Використовується сервер для асинхронної обробки даних Netty (замість звичайного Tomcat Embedded) + Spring WebFlux.
Додатково було додано щось на кшталт конфіг сервера: `config-server`
Загальна структура сервісів виглядає наступним чином:

![img_8.png](.img/lab4/img_8.png)

### Архітектура facade-service (http://localhost:8085)
Мається два ендпоїнти - `POST /facade/write-log` та `GET /facade/logs-messages`

#### POST /facade/write-log
Створює JSON об'єкт, та надсилає його до `POST logging-service/logging`, із наявним механізмом retry (5 спроб через кожні 2 секунди).

![img.png](.img/img.png)

#### GET /facade/logs-messages
Повертає агрегований JSON об'єкт, в якому міститься респонс із `GET logging-service/logging` та `GET messaging-service/messaging`.
`logging-service` повертає масив, тому конкатенація відбувається на рівні фасаду.

### Архітектура logging-service (http://localhost:8090, http://localhost:8091, http://localhost:8092)
Сервіси було зконфігуровано у Hazelcast кластер:

![img.png](.img/lab3/img.png)

Мається два ендпоїнти - `POST /logging` та `GET /logging`

#### POST /logging
Додається новий запис до `IMap` за ключом UUID.
Для дедуплікації запитів (у разі якщо таке стається) використовується UUID із запиту, який служить в якості "Idempotency Key".

![img_1.png](.img/img_1.png)

#### GET /logging
Повертається об'єкт із усіма повідомленнями як JSON array.

### Архітектура messaging-service (http://localhost:8088, http://localhost:8089)
Мається ендпоїнт `GET /messaging`, який повертає всі повідомлення, що були завантажені в мапу в пам'яті.

Консьюмери розподілились таким чином.

![img_9.png](.img/lab4/img_9.png)

#### Завдання

### Записати 10 повідомлень через `facade-service`

![img_10.png](.img/lab4/img_10.png)
![img_11.png](.img/lab4/img_11.png)

### Показати які повідомлення отримав кожен з екземплярів `logging-service` та `facade-service`

#### Розподілення логів

![img_14.png](.img/lab4/img_14.png)
![img_15.png](.img/lab4/img_15.png)
![img_16.png](.img/lab4/img_16.png)

#### Розподілення повідомлень

![img_12.png](.img/lab4/img_12.png)
![img_13.png](.img/lab4/img_13.png)

### Декілька разів викликати HTTP GET на `facade-service` та отримати об'єднані дві множини повідомлень

![img_17.png](.img/lab4/img_17.png)
![img_18.png](.img/lab4/img_18.png)


### Перевірка відмовостійкості черги повідомлень

#### Вимкніть обидва екземпляри `messaging-service`

![img_19.png](.img/lab4/img_19.png)

#### Відправте 10 (буде 12) повідомлень через `facade-service`

![img_21.png](.img/lab4/img_21.png)
![img_20.png](.img/lab4/img_20.png)

#### Вимкніть один із серверів MQ

Спочатку було встановлено 3 партішени, тобто на кожен з них кластер визначає свого лідера. Напевне, для виконання задачі треба було зробити 1 партішен, тому перестворимо топік з цією конфігурацією.

![img_22.png](.img/lab4/img_22.png)

Перезапустимо всю систему та побачимо оновлений кластер. Лідер - `kafka-2`

![img_23.png](.img/lab4/img_23.png)
![img_24.png](.img/lab4/img_24.png)
![img_25.png](.img/lab4/img_25.png)

Вимикаємо `kafka-2`

![img_26.png](.img/lab4/img_26.png)

Запускаємо `messaging-service` та бачимо, що до партішену під'єднався тільки один інстанс, та вичитав всю чергу

![img_27.png](.img/lab4/img_27.png)

Фундаментальне правило Kafka - один `listener` на один партішен на одну консьюмер-групу.
Якщо задати іншому інстансу `messaging-service` іншу групу, то повідомлення будуть прочитані на обох інстансах.

![img_28.png](.img/lab4/img_28.png)