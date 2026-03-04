import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL } from './config.js';

export const options = {
  stages: [
    { duration: '30s', target: 50 },
    { duration: '1m',  target: 100 },
    { duration: '1m',  target: 200 },
    { duration: '30s', target: 0 },
  ],
  // No strict thresholds for stress test — goal is to observe
  thresholds: {
    http_req_duration: ['p(99)<2000'],
  },
};

export default function () {
  const res = http.get(`${BASE_URL}/health`);
  check(res, { 'status ok': (r) => r.status === 200 });
  sleep(1);
}
