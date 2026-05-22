# LinkTracker

LinkTracker – Telegram-бот, который отслеживает изменения на веб-страницах и оперативно информирует пользователя о них.

Это шаблон проекта, который вам необходимо взять за основу для разработки своей системы.
В данном файле должна находиться инструкция для ассистента по запуску и настройке бота.

Полезную для разработки проекта информацию вы можете найти в файле [HELP.md](./HELP.md)

## Инструкция к эксплуатации

Чтобы запустить бота создайте .env в корне и укажите путь до .env в configuration.
Затем поместите TELEGRAM_BOT_TOKEN=${TOKEN} в .env

Альтернатива - просто поместите TELEGRAM_BOT_TOKEN=${TOKEN} в configuration

## Отчет по нагрузочному тестированию

Скрипт тестирования находится в perf-tests/script/test.js

Для тестирования испоьзуются миграции из migrations-k6-test

### Конфигурация теста для 1 минуты

- Кол-во параллельно работающих пользователей: 8
- Ramp-up: 10 секунд
- Длительность теста: 1 минта
- Объем данных:
  - 100 000 ссылок
  - 1000 пользователей (chatId)
  - 100 ссылок на пользователя
- Соотношение операций:
  - GET /links: 99%
  - POST /links: 1%
- Инструмент: K6

<table border="1" cellpadding="8" cellspacing="0">
  <thead>
    <tr bgcolor="#f0f0f0">
      <th><b>Метрика</b></th>
      <th><b>Без кэша</b></th>
      <th><b>С кэшем</b></th>
      <th><b>Изменение</b></th>
    </tr>
  </thead>
  <tbody>
    <tr bgcolor="#e6f2ff">
      <td colspan="4"><b>THRESHOLDS</b></td>
    </tr>
    <tr>
      <td>http_req_duration p(99)</td>
      <td>144.3ms ✓</td>
      <td>47.67ms ✓</td>
      <td bgcolor="#d4edda"><b>-67% ↓</b></td>
    </tr>
    <tr>
      <td>http_req_failed rate</td>
      <td>0.00% ✓</td>
      <td>0.00% ✓</td>
      <td>без изменений</td>
    </tr>
    <tr bgcolor="#e6f2ff">
      <td colspan="4"><b>TOTAL RESULTS</b></td>
    </tr>
    <tr>
      <td>checks_total</td>
      <td>7,272</td>
      <td>27,573</td>
      <td bgcolor="#d4edda"><b>+279% ↑</b></td>
    </tr>
    <tr>
      <td>checks_succeeded</td>
      <td>100.00%</td>
      <td>100.00%</td>
      <td>без изменений</td>
    </tr>
    <tr>
      <td>checks_failed</td>
      <td>0.00%</td>
      <td>0.00%</td>
      <td>без изменений</td>
    </tr>
    <tr>
      <td>GET status 200</td>
      <td>✓</td>
      <td>✓</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>POST status 200</td>
      <td>✓</td>
      <td>✓</td>
      <td>✅</td>
    </tr>
    <tr bgcolor="#e6f2ff">
      <td colspan="4"><b>HTTP</b></td>
    </tr>
    <tr>
      <td>http_req_duration (avg)</td>
      <td>47.87ms</td>
      <td>4.77ms</td>
      <td bgcolor="#d4edda"><b>-90% ↓</b></td>
    </tr>
    <tr>
      <td>http_req_duration (min)</td>
      <td>5.08ms</td>
      <td>424.02µs</td>
      <td bgcolor="#d4edda"><b>-92% ↓</b></td>
    </tr>
    <tr>
      <td>http_req_duration (med)</td>
      <td>30.24ms</td>
      <td>3.06ms</td>
      <td bgcolor="#d4edda"><b>-90% ↓</b></td>
    </tr>
    <tr>
      <td>http_req_duration (max)</td>
      <td>5.13s</td>
      <td>450.5ms</td>
      <td bgcolor="#d4edda"><b>-91% ↓</b></td>
    </tr>
    <tr>
      <td>http_req_duration p(90)</td>
      <td>64ms</td>
      <td>7.5ms</td>
      <td bgcolor="#d4edda"><b>-88% ↓</b></td>
    </tr>
    <tr>
      <td>http_req_duration p(95)</td>
      <td>82.54ms</td>
      <td>10.57ms</td>
      <td bgcolor="#d4edda"><b>-87% ↓</b></td>
    </tr>
    <tr>
      <td>http_req_failed</td>
      <td>0.00%</td>
      <td>0.00%</td>
      <td>без изменений</td>
    </tr>
    <tr>
      <td>http_reqs</td>
      <td>7,272</td>
      <td>27,573</td>
      <td bgcolor="#d4edda"><b>+279% ↑</b></td>
    </tr>
    <tr bgcolor="#e6f2ff">
      <td colspan="4"><b>EXECUTION</b></td>
    </tr>
    <tr>
      <td>iteration_duration (avg)</td>
      <td>5.86s</td>
      <td>1.5s</td>
      <td bgcolor="#d4edda"><b>-74% ↓</b></td>
    </tr>
    <tr>
      <td>iteration_duration (min)</td>
      <td>2.24s</td>
      <td>1.13s</td>
      <td bgcolor="#d4edda"><b>-50% ↓</b></td>
    </tr>
    <tr>
      <td>iteration_duration (med)</td>
      <td>5.45s</td>
      <td>1.51s</td>
      <td bgcolor="#d4edda"><b>-72% ↓</b></td>
    </tr>
    <tr>
      <td>iteration_duration (max)</td>
      <td>9.85s</td>
      <td>2.19s</td>
      <td bgcolor="#d4edda"><b>-78% ↓</b></td>
    </tr>
    <tr>
      <td>iteration_duration p(90)</td>
      <td>9.12s</td>
      <td>1.72s</td>
      <td bgcolor="#d4edda"><b>-81% ↓</b></td>
    </tr>
    <tr>
      <td>iteration_duration p(95)</td>
      <td>9.4s</td>
      <td>1.8s</td>
      <td bgcolor="#d4edda"><b>-81% ↓</b></td>
    </tr>
    <tr>
      <td>iterations</td>
      <td>72</td>
      <td>273</td>
      <td bgcolor="#d4edda"><b>+279% ↑</b></td>
    </tr>
    <tr>
      <td>vus (min/max)</td>
      <td>1 / 8</td>
      <td>1 / 8</td>
      <td>без изменений</td>
    </tr>
    <tr>
      <td>vus_max</td>
      <td>8</td>
      <td>8</td>
      <td>без изменений</td>
    </tr>
    <tr bgcolor="#e6f2ff">
      <td colspan="4"><b>NETWORK</b></td>
    </tr>
    <tr>
      <td>data_received</td>
      <td>103 MB</td>
      <td>389 MB</td>
      <td bgcolor="#d4edda"><b>+278% ↑</b></td>
    </tr>
    <tr>
      <td>data_sent</td>
      <td>669 kB</td>
      <td>2.5 MB</td>
      <td bgcolor="#d4edda"><b>+274% ↑</b></td>
    </tr>
    <tr>
      <td>throughput (received)</td>
      <td>1.7 MB/s</td>
      <td>6.4 MB/s</td>
      <td bgcolor="#d4edda"><b>+276% ↑</b></td>
    </tr>
    <tr>
      <td>throughput (sent)</td>
      <td>11 kB/s</td>
      <td>42 kB/s</td>
      <td bgcolor="#d4edda"><b>+282% ↑</b></td>
    </tr>
    <tr bgcolor="#fff3cd">
      <td colspan="4"><b>ОШИБКИ</b></td>
    </tr>
    <tr>
      <td>сохранение result.json</td>
      <td>❌ no such file</td>
      <td>❌ permission denied</td>
      <td>разные ошибки</td>
    </tr>
  </tbody>
</table>

### Конфигурация теста для 6 минут 30 секунда

- Кол-во параллельно работающих пользователей: 8
- Ramp-up: 1 минута
- Длительность теста: 6 минут 30 секунд
- Объем данных:
  - 100 000 ссылок
  - 1000 пользователей (chatId)
  - 100 ссылок на пользователя
- Соотношение операций:
  - GET /links: 99%
  - POST /links: 1%
- Инструмент: K6

<table border="1" cellpadding="8" cellspacing="0">
  <thead>
    <tr bgcolor="#f0f0f0">
      <th><b>Метрика</b></th>
      <th><b>Без кэша</b></th>
      <th><b>С кэшем</b></th>
      <th><b>Изменение</b></th>
    </tr>
  </thead>
  <tbody>
    <tr bgcolor="#e6f2ff">
      <td colspan="4"><b>THRESHOLDS</b></td>
    </tr>
    <tr>
      <td>http_req_duration p(99)</td>
      <td>94.32ms ✓</td>
      <td>21.78ms ✓</td>
      <td bgcolor="#d4edda"><b>-77% ↓</b></td>
    </tr>
    <tr>
      <td>http_req_failed rate</td>
      <td>0.00% ✓</td>
      <td>0.00% ✓</td>
      <td>без изменений</td>
    </tr>
    <tr bgcolor="#e6f2ff">
      <td colspan="4"><b>TOTAL RESULTS</b></td>
    </tr>
    <tr>
      <td>checks_total</td>
      <td>48,379</td>
      <td>211,595</td>
      <td bgcolor="#d4edda"><b>+337% ↑</b></td>
    </tr>
    <tr>
      <td>checks_succeeded</td>
      <td>100.00%</td>
      <td>100.00%</td>
      <td>без изменений</td>
    </tr>
    <tr>
      <td>checks_failed</td>
      <td>0.00%</td>
      <td>0.00%</td>
      <td>без изменений</td>
    </tr>
    <tr>
      <td>GET status 200</td>
      <td>✓</td>
      <td>✓</td>
      <td>✅</td>
    </tr>
    <tr>
      <td>POST status 200</td>
      <td>✓</td>
      <td>✓</td>
      <td>✅</td>
    </tr>
    <tr bgcolor="#e6f2ff">
      <td colspan="4"><b>HTTP</b></td>
    </tr>
    <tr>
      <td>http_req_duration (avg)</td>
      <td>47.62ms</td>
      <td>3.02ms</td>
      <td bgcolor="#d4edda"><b>-94% ↓</b></td>
    </tr>
    <tr>
      <td>http_req_duration (min)</td>
      <td>3.19ms</td>
      <td>373.27µs</td>
      <td bgcolor="#d4edda"><b>-88% ↓</b></td>
    </tr>
    <tr>
      <td>http_req_duration (med)</td>
      <td>18.29ms</td>
      <td>1.85ms</td>
      <td bgcolor="#d4edda"><b>-90% ↓</b></td>
    </tr>
    <tr>
      <td>http_req_duration (max)</td>
      <td>6.71s</td>
      <td>647.51ms</td>
      <td bgcolor="#d4edda"><b>-90% ↓</b></td>
    </tr>
    <tr>
      <td>http_req_duration p(90)</td>
      <td>36.64ms</td>
      <td>5.53ms</td>
      <td bgcolor="#d4edda"><b>-85% ↓</b></td>
    </tr>
    <tr>
      <td>http_req_duration p(95)</td>
      <td>45.41ms</td>
      <td>7.64ms</td>
      <td bgcolor="#d4edda"><b>-83% ↓</b></td>
    </tr>
    <tr>
      <td>http_req_failed</td>
      <td>0.00%</td>
      <td>0.00%</td>
      <td>без изменений</td>
    </tr>
    <tr>
      <td>http_reqs</td>
      <td>48,379</td>
      <td>211,595</td>
      <td bgcolor="#d4edda"><b>+337% ↑</b></td>
    </tr>
    <tr bgcolor="#e6f2ff">
      <td colspan="4"><b>EXECUTION</b></td>
    </tr>
    <tr>
      <td>iteration_duration (avg)</td>
      <td>5.83s</td>
      <td>1.32s</td>
      <td bgcolor="#d4edda"><b>-77% ↓</b></td>
    </tr>
    <tr>
      <td>iteration_duration (min)</td>
      <td>1.63s</td>
      <td>1.09s</td>
      <td bgcolor="#d4edda"><b>-33% ↓</b></td>
    </tr>
    <tr>
      <td>iteration_duration (med)</td>
      <td>7.31s</td>
      <td>1.31s</td>
      <td bgcolor="#d4edda"><b>-82% ↓</b></td>
    </tr>
    <tr>
      <td>iteration_duration (max)</td>
      <td>9.79s</td>
      <td>2.17s</td>
      <td bgcolor="#d4edda"><b>-78% ↓</b></td>
    </tr>
    <tr>
      <td>iteration_duration p(90)</td>
      <td>8.72s</td>
      <td>1.51s</td>
      <td bgcolor="#d4edda"><b>-83% ↓</b></td>
    </tr>
    <tr>
      <td>iteration_duration p(95)</td>
      <td>9.14s</td>
      <td>1.6s</td>
      <td bgcolor="#d4edda"><b>-82% ↓</b></td>
    </tr>
    <tr>
      <td>iterations</td>
      <td>479</td>
      <td>2,095</td>
      <td bgcolor="#d4edda"><b>+337% ↑</b></td>
    </tr>
    <tr>
      <td>vus (min/max)</td>
      <td>1 / 8</td>
      <td>1 / 8</td>
      <td>без изменений</td>
    </tr>
    <tr>
      <td>vus_max</td>
      <td>8</td>
      <td>8</td>
      <td>без изменений</td>
    </tr>
    <tr bgcolor="#e6f2ff">
      <td colspan="4"><b>NETWORK</b></td>
    </tr>
    <tr>
      <td>data_received</td>
      <td>682 MB</td>
      <td>3.0 GB</td>
      <td bgcolor="#d4edda"><b>+350% ↑</b></td>
    </tr>
    <tr>
      <td>data_sent</td>
      <td>4.5 MB</td>
      <td>20 MB</td>
      <td bgcolor="#d4edda"><b>+344% ↑</b></td>
    </tr>
    <tr>
      <td>throughput (received)</td>
      <td>1.7 MB/s</td>
      <td>7.7 MB/s</td>
      <td bgcolor="#d4edda"><b>+353% ↑</b></td>
    </tr>
    <tr>
      <td>throughput (sent)</td>
      <td>11 kB/s</td>
      <td>50 kB/s</td>
      <td bgcolor="#d4edda"><b>+355% ↑</b></td>
    </tr>
  </tbody>
</table>

Вывод: двух уровневный кэш (локальный + редис) просто имба
