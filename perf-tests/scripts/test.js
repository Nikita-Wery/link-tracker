import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    scenarios: {
        load_test: {
            executor: 'ramping-vus',


            // stages: [
            //     { duration: '10s', target: 8 },
            //     { duration: '40s', target: 8 },
            //     { duration: '10s', target: 0 },
            // ],
            stages: [
                { duration: '1m', target: 8 },
                { duration: '5m', target: 8 },
                { duration: '30s', target: 0 },
            ],
        },
    },

    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(99)<1000'],
    },
};

const BASE_URL = 'http://scrapper:8081';

function randomChatId() {
    return Math.floor(Math.random() * 1000) + 1;
}

function randomUrl() {
    const repoId = Math.floor(Math.random() * 900000) + 100001;

    return `https://github.com/user/repo${repoId}`;
}

export default function () {

    const chatId = randomChatId();

    for (let i = 0; i < 100; i++) {

        const res = http.get(
            `${BASE_URL}/links`,
            {
                headers: {
                    'Tg-Chat-Id': String(chatId),
                },
            }
        );

        check(res, {
            'GET status 200': (r) => r.status === 200,
        });
    }

    const payload = JSON.stringify({
        link: randomUrl(),
        tags: ['test'],
    });

    const postRes = http.post(
        `${BASE_URL}/links`,
        payload,
        {
            headers: {
                'Content-Type': 'application/json',
                'Tg-Chat-Id': String(chatId),
            },
        }
    );

    check(postRes, {
        'POST status 200': (r) => r.status === 200,
    });

    sleep(1);
}
