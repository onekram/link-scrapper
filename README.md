![Build](https://github.com/central-university-dev/backend-academy-2025-spring-template/actions/workflows/build.yaml/badge.svg)

# Link Tracker

<!-- этот файл можно и нужно менять -->

Проект сделан в рамках курса Академия Бэкенда.

Приложение для отслеживания обновлений контента по ссылкам.
При появлении новых событий отправляется уведомление в Telegram.

Проект написан на `Java 23` с использованием `Spring Boot 3`.

Проект состоит из 2-х приложений:
* Bot
* Scrapper

Для работы требуется БД `PostgreSQL`. Присутствует опциональная зависимость на `Kafka`.

Для дополнительной справки: [HELP.md](./HELP.md)

## Запуск на локальной машине
- Запуск [bot](bot/src/main/java/backend/academy/bot/BotApplication.java)
  - Указать переменную окружения *TELEGRAM_TOKEN*
  - Запустить SpringBootApplication
- Запуск [scrapper](scrapper/src/main/java/backend/academy/scrapper/ScrapperApplication.java)
 - Указать переменную окружения *SO_ACCESS_TOKEN*
 - Указать переменную окружения *SO_TOKEN_KEY*
 - Запустить SpringBootApplication
