# migration

Шаги для запуска миграции:
1. в папке debug/docker-compose.yaml запускаем pg (image: postgres:13.4) 
2. в классе kz/greetgo/sandboxserver/migration/GenerateInputFiles.java запускаем метод main для генераций файлов (около 5 минут)
3. После генраций, в классе kz/greetgo/sandboxserver/migration/LaunchMigration.java запускаем main метод

Обязательные creditionals чтобы подключиться к postgresql:
user: postgres
password: pass123
database: sandbox_db
URL: jdbc:postgresql://localhost:12218/sandbox_db

Во время миграции будет происходить
1. разархивация файлов
2. создание TMP таблицы
3. загрузка данных в TMP
4. валидация данных
5. создание Actual Tables
6. Загрузка данных в Actual Tables
7. Загрузка ошибок из TMP в лог файл
8. Проверка на равное количество ошибок в лог файле и в TMP 

Тесты находятся в папке kz/greetgo/sandboxserver/migration/

MySAXHandlerTest - тесты для парсера

CiaMigrationTest - тесты для валидации и методов данного класса

testXml100 - папка содержащая тестовый xml для проверки download метода
