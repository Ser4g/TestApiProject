# Чек-лист тест-кейсов для API `/user/create` и `/user/get`

## 1. Функциональные проверки

| № | Ручка | Название проверки | Приоритет |
|---|---|---|---|
| 1 | /user/create | Успешная регистрация с валидными username, email, password → 200, success=true, details с полями id, username, email, password, created_at, updated_at | Critical |
| 2 | /user/create | Пароль в ответе хеширован, не возвращается в открытом виде | Critical |
| 3 | /user/create | Отсутствует поле username → 400, success=false, «A username is required» | Critical |
| 4 | /user/create | Отсутствует поле email → 400, success=false, «An Email is required» | Critical |
| 5 | /user/create | Отсутствует поле password → 400, success=false, «A password is required» | Critical |
| 6 | /user/create | Пустые значения полей (username="", email="", password="") → 400, success=false, «A username is required» | Critical |
| 7 | /user/create | Регистрация с уже занятым username → 400, success=false, «This username is taken. Try another.» | Critical |
| 8 | /user/create | Регистрация с уже занятым email → 400, success=false, «Email already exists» | Critical |
| 9 | /user/get | GET /user/get → 200, возвращается массив пользователей | Critical |
| 10 | /user/get | Структура каждого объекта: id, username, email, password, created_at, updated_at — все поля присутствуют | Critical |
| 11 | /user/get | Пароль в списке хеширован, не возвращается в открытом виде | Critical |
| 12 | обе | create → get → пользователь присутствует с корректными данными | Critical |
| 13 | обе | Несколько последовательных регистраций: все пользователи в /user/get, с корректными username/email (те которые отправлены) и id корректны и уникальны (которые были получены при создании) и id инкрементируется | Critical |
| 14 | обе | create (ошибка) → get: пользователь НЕ добавлен в список | Critical |
| 15 | /user/create | created_at и updated_at заполнены и совпадают при создании; формат «YYYY-MM-DD HH:MM:SS» по UTC | High |
| 16 | /user/create | Невалидный формат email (без @, «test@», «@test.ru») → 400, success=false, текст ошибки | High |
| 17 | /user/create | Email в другом регистре (Test@Test.ru vs test@test.ru) — регистрозависимость проверки уникальности → 400, success=false, «Email already exists» | High |
| 18 | /user/create | Username в разном регистре (Test vs test) — регистрозависимость проверки уникальности → 400, success=false, «This username is taken. Try another.» | High |
| 19 | /user/create | Граничные значения длины: username/email/password минимальной и максимальной длины (требуется уточнить требования где будет указаны значения макс/мин) | High |
| 20 | /user/create | Неверный Content-Type (JSON вместо multipart/form-data) → 415, Unsupported Media Type | High |
| 21 | /user/create | Запрос методом GET/PUT/DELETE/PATCH на /user/create → 405, Method not allowed | High |
| 22 | /user/get | Пустая БД → 200 и пустой массив [] | High |
| 23 | /user/get | Запрос методом POST/PUT/DELETE/PATCH на /user/get → 405 | High |
| 24 | /user/create | Лишние (необязательные) поля в FormData → игнорируются, 200, success=true, details со всеми полями | Medium |
| 25 | /user/create | Username с пробелами, спецсимволами, кириллицей — (требуется уточнить требования по сохранению) | Medium |
| 26 | /user/create | Пробелы в начале/конце значений (username=" test ") — (требуется уточнить требования по сохранению) | Medium |
| 27 | /user/create | Password с спецсимволами, кириллицей, emoji — (требуется уточнить требования по сохранению) | Medium |
| 28 | /user/get | created_at/updated_at в валидном формате и created_at равен дате создания. | Medium |

## 2. Нефункциональные проверки

### 2.1. Безопасность

| № | Ручка | Название проверки | Приоритет |
|---|---|---|---|
| 1 | /user/create | SQL-инъекция в username/email (например "admin'--", "'; DROP TABLE users;--") → безопасная обработка, без 500 и без влияния на БД | Critical |
| 2 | /user/get | SQL-инъекция в параметрах запроса (если вдруг появятся) → безопасная обработка | High |
| 3 | /user/create | XSS-пейлоад в username ("<script>alert(1)</script>") — сохраняется как данные, не исполняется | High |
| 4 | обе | Авторизация: доступ к /user/get без токена — если вдруг планируется в дальнейшем | High |
| 5 | обе | Заголовки безопасности в ответе (отсутствие лишних заголовков, X-Powered-By и т.п.) | Medium |
| 6 | обе | Rate limiting: ограничения на частоту запросов (brute-force защита) | Medium |

### 2.2. Нагрузка и производительность

| № | Ручка | Название проверки | Приоритет |
|---|---|---|---|
| 1 | обе | Время ответа при нормальной нагрузке (уточнить требования к допустимой времени ответа) | High |
| 2 | /user/get | Время ответа при большом объёме данных | High |
| 3 | обе | Стресс-тест: пиковая нагрузка (например, 100 RPS в течение 5 мин) — нет 5xx, деградация в норме | Medium |
| 4 | /user/create | Одновременная регистрация двух пользователей с одинаковым username → только один успешен | Medium |
| 5 | обе | Тест на выносливость: длительная умеренная нагрузка — нет утечек памяти и деградации | Medium |

### 2.3. Надёжность и отказоустойчивость

| № | Ручка | Название проверки | Приоритет |
|---|---|---|---|
| 1 | /user/create | Перезапуск сервера: данные пользователей сохраняются, /user/get возвращает актуальный список | High |
| 2 | /user/create | Ошибка БД (недоступна БД) → 500 с сообщением об ошибке, а не падение сервера | Medium |

### 2.4. Удобство использования API

| № | Ручка | Название проверки | Приоритет |
|---|---|---|---|
| 1 | обе | Сообщения об ошибках понятны и позволяют клиенту понять, что именно исправить | Medium |
| 2 | обе | Документация (Swagger/OpenAPI) соответствует фактическому поведению API | Medium |

---

**Итого:** 28 функциональных и 15 нефункциональных проверок.
