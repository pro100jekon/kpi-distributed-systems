### Попередні дані
Для запуску Management Center:
```shell
docker run --rm -p 8080:8080 --network bridge hazelcast/management-center:5.3.4
```
Для запуску програми з декількох Hazelcast нод:
```shell
# from ./hazelcast folder
docker build -t kpi-hazelcast:latest
docker run --rm -p 1998:1998 -p 1999:1999 -p 2000:2000 --network bridge kpi-hazelcast:latest
```
### Звіт
Логи інстансу

![img_1.png](.img/img_1.png)

#### Використовуючи API, створіть Distributed Map, та запишіть в неї 1000 значень з ключами від [0,1000)
![img.png](.img/img.png)
![img_2.png](.img/img_2.png)

Код програми використовував тільки одну ноду Hazelcast, на порті 2000.
#### За допомогою Management Center подивіться на розподіл ключів по нодах

Налаштування кластеру.

![img_3.png](.img/img_3.png)
![img_4.png](.img/img_4.png)
![img_5.png](.img/img_5.png)
![img_6.png](.img/img_6.png)
Приблизно рівномірний розподіл значень, хоч і використовувалась тільки одна нода для додавання нових ключів.
#### Подивіться, як зміниться розподіл даних по нодах, якщо відключити одну ноду

Для шатдауну ноди можна використати той самий Management Center. Далі наведений приклад з уже вимкненою нодою.

![img_7.png](.img/img_7.png)

Володіння ключами перейшло на інші ноди

#### Послідовне вимкнення двох нод

Вимкнемо 2000 порт...
![img_8.png](.img/img_8.png)

Все збережено.

#### Паралельне вимкнення двох нод

Для цього треба перезапустити інстанси в docker-compose.

Додавання інстансів із compose.
![img_9.png](.img/img_9.png)
![img_10.png](.img/img_10.png)
![img_11.png](.img/img_11.png)
![img_12.png](.img/img_12.png)

Призупинимо два інстанси Hazelcast.
```shell
docker ps
```
![img_13.png](.img/img_13.png)
```shell
docker kill -s KILL hz-1 hz-2
```
![img_14.png](.img/img_14.png)
![img_15.png](.img/img_15.png)

Відбулася втрата даних. Для попередження цієї ситуації треба підняти кількість бекапів у конфігурації мапи.
![img_17.png](.img/img_17.png)
![img_16.png](.img/img_16.png)

Хоч менеджер і показує "Entries" з розподілом, але дані все одно зберігаються при паралельному вимкненні нод.
![img_18.png](.img/img_18.png)

### Distributed Map
#### Без блокування
![img_19.png](.img/img_19.png)
![img_20.png](.img/img_20.png)
![img_21.png](.img/img_21.png)

#### Песимістичне блокування
![img_22.png](.img/img_22.png)
![img_23.png](.img/img_23.png)
![img_24.png](.img/img_24.png)

#### Оптимістичне блокування
![img_25.png](.img/img_25.png)
![img_26.png](.img/img_26.png)
![img_27.png](.img/img_27.png)

Песимістичне блокування помітно повільніше, аніж оптимістичне.

### Bounded Queue

#### Приклад "нормальної роботи". Вичитування проходить рівномірно.
![img_30.png](.img/img_30.png)
![img_28.png](.img/img_28.png)
![img_29.png](.img/img_29.png)

#### Приклад роботи інстанса, що наповнює чергу, але немає читання.
![img_31.png](.img/img_31.png)
![img_32.png](.img/img_32.png)

І зависло. Програма нескінченно чекає на момент, коли черга звільниться.
![img_33.png](.img/img_33.png)