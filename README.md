# BOKT-next

Переписанная с нуля система мгновенных кредитов (аналог «Ani Kredit» / BOKT) на **Java 21 + Spring Boot 3**.
Модульный монолит: один Gradle-проект, внутри — независимые модули с чёткими границами.

> ⚠️ **Важно про сборку.** Проект писался без доступа к сети, поэтому `gradle build` и
> компиляция **не запускались**. Перед первым запуском собери локально
> (`./gradlew build`) и поправь версии зависимостей под свой репозиторий при необходимости.
> Версии заданы в `gradle.properties` (Spring Boot 3.3.4) — при желании подними до актуальных.

## Модули

| Модуль          | Назначение |
|-----------------|-----------|
| `common`        | Базовые сущности, обработка ошибок, `TenantContext`, `TenantLookup` (порт), утилиты |
| `auth`          | Пользователи, роли, права, JWT (RS256 access + opaque refresh), двухшаговый вход (пароль + OTP) |
| `tenant`        | Организации (NBCO) = тенанты, карты, валюты, BIN-ы, филиалы, регистрация NBCO |
| `client`        | Клиенты-заёмщики |
| `credit`        | **Кредиты + платежи в одном модуле**: жизненный цикл кредита, CHARGE/TOPUP, reversal, порт процессинга |
| `notification`  | Порт SMS + шаблоны уведомлений (OTP, учётные данные, выдача кредита) |
| `file-import`   | Загрузка кредитов из CSV |
| `audit`         | Журнал операций + трассировка по correlationId |
| `api`           | Точка входа, мультитенантный аспект, correlation-id фильтр, конфиг, Flyway-миграции |

Граф зависимостей (без циклов): `common` ← все; `auth` ← common, notification;
`tenant` ← common, auth; `client` ← common, auth; `credit` ← common, auth, tenant, client, notification, audit;
`file-import` ← common, credit, client; `api` ← все.

## Ключевые архитектурные решения

- **Свой auth вместо Keycloak.** Spring Security + JWT. Access-токен — JWT RS256 (публичный ключ
  можно отдавать другим сервисам для верификации). Refresh-токен — непрозрачный, хранится хэшем,
  с ротацией и отзывом. Вход двухшаговый: `login` (orgLogin + username + password) → OTP по SMS →
  `verify-otp` → пара токенов.
- **Мультитенантность** — shared schema + `tenant_id`. Изоляция чтения через Hibernate-фильтр
  `tenantFilter`, который включается автоматически аспектом `TenantFilterAspect` внутри транзакции.
  `tenant_id` при вставке проставляется автоматически из `TenantContext`. Супер-админ платформы
  (требование ЦБ) получает кросс-тенантный доступ — фильтр для него отключён.
- **Деньги — в минорных единицах** (`long`, см. `Money`). Устраняет баг округления старого BOKT
  (286.40 → 286.39): арифметика с плавающей точкой заменена на целочисленную.
- **Процессинг — за портом** (`ProcessingClient`). Приложение не знает деталей карточного
  процессинга. Сейчас включена заглушка `StubProcessingClient` (возвращает отказ). См. ниже.
- **Correlation ID** в каждой строке лога и в аудите — конец эпохи ручного `grep` по RRN.
- **Пароли** — делегирующий энкодер (bcrypt по умолчанию, с возможностью апгрейда на argon2),
  вместо sha256-из-онлайн-сервиса и md5 из старого BOKT.

## Запуск

### 1. База данных (PostgreSQL)

```bash
docker run --name bokt-pg -e POSTGRES_DB=bokt -e POSTGRES_USER=bokt \
  -e POSTGRES_PASSWORD=bokt -p 5432:5432 -d postgres:16
```

Схему создаст Flyway при старте приложения (миграции V1–V7).

### 2. Переменные окружения

| Переменная | Назначение | По умолчанию |
|------------|-----------|--------------|
| `BOKT_DB_URL` | JDBC-URL | `jdbc:postgresql://localhost:5432/bokt` |
| `BOKT_DB_USER` / `BOKT_DB_PASSWORD` | доступ к БД | `bokt` / `bokt` |
| `BOKT_JWT_PRIVATE_KEY` / `BOKT_JWT_PUBLIC_KEY` | PEM-ключи для подписи JWT | если пусто — генерится временная пара (только dev) |
| `bokt.bootstrap.super-admin-password` | пароль супер-админа при первом старте | если пусто — генерится и печатается в лог один раз |

### 3. Сборка и старт

```bash
./gradlew :api:bootRun
# или
./gradlew build && java -jar api/build/libs/bokt-next.jar
```

При первом старте `DataInitializer` создаёт платформенный тенант (`login=platform`) и супер-админа
(`username=superadmin`). Пароль — из конфига или сгенерированный (смотри лог, строка «Создан супер-админ платформы»).

### 4. Первые шаги по API

```
POST /api/auth/login        { "orgLogin":"platform", "username":"superadmin", "password":"..." }
  → { reference, otpTtlSeconds }         (OTP уходит по SMS; в dev — печатается в лог заглушкой SMS)
POST /api/auth/verify-otp   { "reference":"...", "code":"123456" }
  → { accessToken, refreshToken, ... }
POST /api/organisations     (Bearer accessToken)   — регистрация новой NBCO
```

## Как подключить свой процессинг

Реализуй интерфейс `az.bokt.credit.processing.ProcessingClient` и объяви его Spring-бином —
заглушка автоматически отключится (`@ConditionalOnMissingBean`).

```java
@Component
public class RealProcessingClient implements ProcessingClient {
    @Override public ProcessingResult charge(ProcessingRequest r) { /* отправка на процессинг */ }
    @Override public ProcessingResult topup(ProcessingRequest r)  { /* посадка денег на карту клиента */ }
    @Override public ProcessingResult reverse(ReversalRequest r)  { /* возврат */ }
}
```

Контракт:
- Возвращай `ProcessingResult.Approved(rrn, approvalCode)` при успехе, иначе
  `ProcessingResult.Declined(code, rawMessage)`.
- Маппинг E-кодов процессинга — в `ProcessingErrorCode.fromRaw(...)`
  (E000004 → CARD_LOCKED, E010000 → DUPLICATE_REFERENCE, E000001 → INTERNAL, E010002 → INSUFFICIENT_FUNDS).
- Операции должны быть **идемпотентны по `guid`/`reference`**: повторный запрос с тем же GUID
  обязан вернуть исходный результат (дубликат сигнализируется кодом E010000).
- GUID формируются как в BOKT: CHARGE = `"CH" + creditId`, TOPUP = `"TP" + creditId`.

Оркестрацию (CHARGE с карты NBCO → TOPUP на карту клиента, компенсирующий reversal при сбое TOPUP)
менять не нужно — она в `CreditPaymentService`.

Заглушку SMS (`LoggingSmsSender`) точно так же заменяет реальный бин `SmsSender`.

## Основные эндпоинты

```
POST /api/auth/login | /verify-otp | /refresh | /logout        аутентификация
GET  /api/auth/me                                               текущий пользователь
POST /api/organisations                                         регистрация NBCO (супер-админ)
POST /api/branches, GET /api/branches                           филиалы
POST /api/roles, GET /api/roles                                 роли и права
POST /api/users, .../{id}/disable|enable|reset-password         пользователи NBCO
POST /api/clients, PUT /api/clients/{id}, GET /api/clients      клиенты
POST /api/credits                                               создать заявку (оператор)
POST /api/credits/{id}/approve|cancel|postpone                  жизненный цикл
GET  /api/credits, /{id}, /by-pan, /loaded, /archive           просмотр/отчёты
POST /api/credits/{id}/reversal                                 возврат (полный/частичный)
POST /api/credit-files                                          импорт кредитов из CSV
GET  /api/audit/by-correlation/{id}, /entity/{type}/{id}        журнал аудита
```

## Что осталось на потом

- Реальные реализации `ProcessingClient` и `SmsSender`.
- Прогнать `./gradlew build` и починить возможные мелочи компиляции/версий (сеть была недоступна).
- Тесты (Testcontainers уже подключён в `api`).
- Дособрать аудит-события в бизнес-операциях (сервис `AuditService` готов, точки вызова добавляются по вкусу).
