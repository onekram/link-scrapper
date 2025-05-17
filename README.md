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
### Переменные окружения

###### Креды для postgres
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PORT`
- `POSTGRES_PASSWORD`

###### Креды для redis
- `REDIS_PASSWORD`
- `REDIS_PORT`
- `REDIS_HOST`

###### Ключи и токены
- `GITHUB_TOKEN` - токен для доступа к API Github
- `SO_TOKEN_KEY` - ключ для доступа к API StackOverflow
- `SO_ACCESS_TOKEN` - токен для доступа к API StackOverflow
- `TELEGRAM_TOKEN` - токен для доступа к API Telegram

###### Внешние сервисы
- `BOT_URL` - http://localhost:8080
- `SCRAPPER_URL` - http://localhost:8081
- `SO_API_URL` - https://api.stackexchange.com/2.3
- `GITHUB_API_URL`- https://api.github.com
### Запуск контейнеров

```bash
docker compose --env-file .env -f docker/docker-compose.yml -p link-tracker up -d
```

### Запуск приложений
- Запуск [bot](bot/src/main/java/backend/academy/bot/BotApplication.java)
- Запуск [scrapper](scrapper/src/main/java/backend/academy/scrapper/ScrapperApplication.java)
