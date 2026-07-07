import http from 'k6/http';
import { check, fail } from 'k6';
import { businessErrors, config } from './config.js';

function buildHeaders(token, extraHeaders = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...extraHeaders,
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  return headers;
}

function parseBody(response) {
  try {
    return response.json();
  } catch (_) {
    return null;
  }
}

export function request(method, path, { token, body, params = {}, expectedStatus, tags = {} } = {}) {
  const url = `${config.baseUrl}${path}`;
  const response = http.request(method, url, body ? JSON.stringify(body) : null, {
    headers: buildHeaders(token),
    tags,
    ...params,
  });

  const success = check(response, {
    [`${method} ${path} -> ${expectedStatus}`]: (res) => res.status === expectedStatus,
  });

  if (!success) {
    businessErrors.add(1);
    fail(`Unexpected status for ${method} ${path}: ${response.status} body=${response.body}`);
  }

  businessErrors.add(0);
  return {
    response,
    payload: parseBody(response),
  };
}

export function getData(method, path, options) {
  const { payload } = request(method, path, options);
  return payload?.data;
}
