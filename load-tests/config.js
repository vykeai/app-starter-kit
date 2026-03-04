export const BASE_URL = __ENV.BASE_URL || 'http://localhost:3000';

export const thresholds = {
  http_req_duration: ['p(95)<500', 'p(99)<1000'],
  http_req_failed: ['rate<0.01'],
};
