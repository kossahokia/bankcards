💳 BankCards REST API

Backend-приложение для управления банковскими картами
Позволяет пользователям просматривать свои карты, делать переводы между ними, а администраторам — управлять пользователями и картами.
Реализовано на Spring Boot 3 + Java 21 + PostgreSQL + JWT Security.

🚀 Запуск проекта
🔧 Требования

Java 21+
Docker & Docker Compose
Maven 3.9+

▶️ Быстрый запуск через Docker Compose:
git clone https://github.com/kossahokia/bankcards.git
cd bankcards
docker compose up --build

После успешного запуска:
| Сервис        | URL                                                                                        |
| ------------- | ------------------------------------------------------------------------------------------ |
| 🧠 API        | [http://localhost:8080](http://localhost:8080)                                             |
| 📘 Swagger UI | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |
| 🗄️ PgAdmin   | [http://localhost:8081](http://localhost:8081)                                             |

Данные для входа в PgAdmin:
Email: kostya4j@gmail.com
Пароль: postgres

🧱 Архитектура проекта
src/
├── main/java/com/example/bankcards/
│   ├── controller/        # REST-контроллеры (Auth, Admin, Card)
│   ├── service/           # Бизнес-логика и операции
│   ├── entity/            # JPA-сущности (User, Role, Card)
│   ├── repository/        # Spring Data JPA репозитории
│   ├── security/          # JWT, фильтры, UserDetailsServiceImpl
│   ├── config/            # Security, Locale, OpenAPI, Swagger
│   └── util/              # Шифрование, маскирование, валидация
│
├── main/resources/
│   ├── application.yml           # Конфигурация среды
│   ├── db/migration/             # Liquibase миграции
│   └── docs/openapi.yaml         # OpenAPI спецификация
│
└── test/
├── ...                       # Unit и Integration тесты
└── resources/application-test.yml

🔐 Безопасность:
JWT (JSON Web Token) — аутентификация без сессий
Spring Security 6 — разграничение доступа
BCrypt — безопасное хранение паролей
CORS — включён в SecurityConfig

Роли:
ADMIN — полное управление пользователями и картами
USER — просмотр и переводы между своими картами

Администратор по умолчанию (из миграций Liquibase):
username: admin
password: admin123

💳 Основные функции API:
🧩 AuthController (/api/auth):
| Метод  | Endpoint    | Описание                        |
| ------ | ----------- | ------------------------------- |
| `POST` | `/login`    | Аутентификация, получение JWT   |
| `POST` | `/register` | Регистрация нового пользователя |

👑 AdminController (/api/admin) (только для ADMIN):
Пользователи:
| Метод    | Endpoint                                | Описание                                            |
| -------- | --------------------------------------- | --------------------------------------------------- |
| `POST`   | `/users`                                | Создание пользователя                               |
| `GET`    | `/users`                                | Просмотр пользователей (с фильтрацией и пагинацией) |
| `GET`    | `/users/{id}`                           | Получить пользователя по ID                         |
| `PATCH`  | `/users/{id}/status?enabled=true`       | Активировать / деактивировать                       |
| `PATCH`  | `/users/{id}/role?roleName=USER`        | Назначить роль                                      |
| `PATCH`  | `/users/{id}/role/remove?roleName=USER` | Удалить роль                                        |
| `DELETE` | `/users/{id}`                           | Удалить пользователя                                |
| `GET`    | `/users/{id}/cards`                     | Получить карты пользователя                         |

Карты:
| Метод    | Endpoint                            | Описание                                 |
| -------- | ----------------------------------- | ---------------------------------------- |
| `POST`   | `/cards`                            | Создать новую карту                      |
| `GET`    | `/cards`                            | Список всех карт (с фильтром по статусу) |
| `PATCH`  | `/cards/{id}/status?status=BLOCKED` | Изменить статус карты                    |
| `DELETE` | `/cards/{id}`                       | Удалить карту                            |

💼 CardController (/api/cards):
| Метод  | Endpoint              | Описание                                |
| ------ | --------------------- | --------------------------------------- |
| `GET`  | `/`                   | Просмотр своих карт (фильтр, пагинация) |
| `GET`  | `/{id}/balance`       | Баланс карты                            |
| `POST` | `/{id}/request-block` | Запрос на блокировку карты              |
| `POST` | `/transfer`           | Перевод между своими картами            |

🧾 Модели данных:
User:
| Поле     | Тип       | Описание                 |
| -------- | --------- | ------------------------ |
| id       | Long      | ID пользователя          |
| username | String    | Уникальный логин         |
| password | String    | Хэшированный пароль      |
| fullName | String    | Полное имя               |
| enabled  | boolean   | Активен / заблокирован   |
| roles    | Set<Role> | Список ролей             |
| cards    | Set<Card> | Список карт пользователя |

Card:
| Поле     | Тип       | Описание                 |
| -------- | --------- | ------------------------ |
| id       | Long      | ID пользователя          |
| username | String    | Уникальный логин         |
| password | String    | Хэшированный пароль      |
| fullName | String    | Полное имя               |
| enabled  | boolean   | Активен / заблокирован   |
| roles    | Set<Role> | Список ролей             |
| cards    | Set<Card> | Список карт пользователя |

Role:
| Поле | Тип    | Описание                        |
| ---- | ------ | ------------------------------- |
| id   | Long   | ID роли                         |
| name | String | Название роли (`USER`, `ADMIN`) |

🔒 Шифрование и маскирование:
Все номера карт шифруются с помощью AES-128 (EncryptionUtil)
Для отображения в API используются маски:
**** **** **** 1234
Проверка срока действия карты выполняется в CardExpiryUtil

🧠 Конфигурация:
application.yml
PostgreSQL (через Docker)
Liquibase миграции
JWT настройки (secret, expiration)
Логирование уровней: WARN / INFO
application-test.yml
Testcontainers (PostgreSQL 16)
Liquibase отключён
Автогенерация схемы Hibernate

🧪 Тестирование:
| Тип                  | Кол-во                                      | Описание                    |
| -------------------- | ------------------------------------------- | --------------------------- |
| **Unit-тесты**       | ~120                                        | Сервисы и контроллеры       |
| **Integration-тест** | 1                                           | Smoke-тест с Testcontainers |
| **Фреймворки**       | JUnit 5, Mockito, AssertJ, Spring Boot Test |                             |

Запуск тестов:
mvn clean test

🐳 Docker окружение:
docker-compose.yml поднимает:
| Сервис        | Версия | Порт |
| ------------- | ------ | ---- |
| PostgreSQL    | 16     | 5432 |
| PgAdmin       | 8      | 8081 |
| BankCards App | —      | 8080 |

📘 Документация API:
Swagger UI: http://localhost:8080/swagger-ui/index.html
OpenAPI спецификация: src/main/resources/docs/openapi.yaml
Все эндпоинты задокументированы аннотациями:
@Operation, @ApiResponses, @Schema
Отображаются коды ошибок (400, 401, 403, 404, 422, 500)

🧾 Технологии:
| Категория | Технология                       |
| --------- | -------------------------------- |
| Backend   | Java 21, Spring Boot 3.3         |
| Security  | Spring Security 6, JWT           |
| DB        | PostgreSQL 16, Liquibase         |
| Docs      | Swagger / OpenAPI 3              |
| Testing   | JUnit 5, Mockito, Testcontainers |
| DevOps    | Docker, Docker Compose           |
| Utils     | AES Encryption, Card masking     |

👨‍💻 Автор:
Konstantin Sahokiia
📧 kostya4j@gmail.com
📧 kos.sahokia@yandex.ru
🐙 github.com/kossahokia
💬 Telegram: @kossahokia