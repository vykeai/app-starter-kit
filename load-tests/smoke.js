import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, thresholds } from './config.js';

export const options = {
  vus: 1,
  duration: '30s',
  thresholds,
};

export default function () {
  // Health check
  const health = http.get(`${BASE_URL}/health`);
  check(health, { 'health is 200': (r) => r.status === 200 });

  // Version check
  const version = http.get(`${BASE_URL}/api/v1/app/version-check?platform=ios&version=1.0.0`);
  check(version, { 'version check is 200': (r) => r.status === 200 });

  // Magic link request (rate limited, so expect 200 or 429)
  const magicLink = http.post(
    `${BASE_URL}/api/v1/auth/magic-link/request`,
    JSON.stringify({ email: `load-test-${__VU}@example.com` }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  check(magicLink, { 'magic link accepted or rate limited': (r) => r.status === 200 || r.status === 201 || r.status === 429 });

  sleep(1);
}
