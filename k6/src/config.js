import { Rate } from 'k6/metrics';

export const businessErrors = new Rate('business_errors');

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SCENARIO = (__ENV.SCENARIO || 'all').toLowerCase();

export const config = {
  baseUrl: BASE_URL.replace(/\/$/, ''),
  scenarioSelection: SCENARIO,
  credentials: {
    admin: {
      email: __ENV.ADMIN_EMAIL || 'admin@shop.com',
      password: __ENV.SEED_PASSWORD || '123456',
    },
    receptionist: {
      email: __ENV.RECEPTIONIST_EMAIL || 'receptionist@shop.com',
      password: __ENV.SEED_PASSWORD || '123456',
    },
    mechanic: {
      email: __ENV.MECHANIC_EMAIL || 'mechanic@shop.com',
      password: __ENV.SEED_PASSWORD || '123456',
    },
    warehouse: {
      email: __ENV.WAREHOUSE_EMAIL || 'warehouse@shop.com',
      password: __ENV.SEED_PASSWORD || '123456',
    },
  },
};

function defaultThresholds() {
  return {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<2000', 'p(99)<4000'],
    business_errors: ['rate<0.10'],
  };
}

export function buildOptions() {
  const thresholds = defaultThresholds();
  const scenarios = {};

  if (config.scenarioSelection === 'all' || config.scenarioSelection === 'baseline') {
    scenarios.baseline = {
      executor: 'ramping-vus',
      exec: 'baseline',
      startVUs: Number(__ENV.BASELINE_START_VUS || 1),
      stages: [
        { duration: __ENV.BASELINE_RAMP_UP || '1m', target: Number(__ENV.BASELINE_TARGET_VUS || 10) },
        { duration: __ENV.BASELINE_HOLD || '3m', target: Number(__ENV.BASELINE_TARGET_VUS || 10) },
        { duration: __ENV.BASELINE_RAMP_DOWN || '30s', target: 0 },
      ],
      gracefulRampDown: '30s',
      tags: { test_type: 'baseline' },
    };
  }

  if (config.scenarioSelection === 'all' || config.scenarioSelection === 'stress') {
    scenarios.stress = {
      executor: 'ramping-vus',
      exec: 'stress',
      startVUs: Number(__ENV.STRESS_START_VUS || 5),
      stages: [
        { duration: __ENV.STRESS_RAMP_1 || '2m', target: Number(__ENV.STRESS_TARGET_1 || 20) },
        { duration: __ENV.STRESS_RAMP_2 || '2m', target: Number(__ENV.STRESS_TARGET_2 || 40) },
        { duration: __ENV.STRESS_RAMP_3 || '2m', target: Number(__ENV.STRESS_TARGET_3 || 60) },
        { duration: __ENV.STRESS_HOLD || '3m', target: Number(__ENV.STRESS_TARGET_3 || 60) },
        { duration: __ENV.STRESS_RAMP_DOWN || '1m', target: 0 },
      ],
      gracefulRampDown: '45s',
      tags: { test_type: 'stress' },
    };
  }

  if (config.scenarioSelection === 'all' || config.scenarioSelection === 'spike') {
    scenarios.spike = {
      executor: 'ramping-vus',
      exec: 'spike',
      startVUs: 0,
      stages: [
        { duration: __ENV.SPIKE_RAMP_UP || '10s', target: Number(__ENV.SPIKE_TARGET || 80) },
        { duration: __ENV.SPIKE_HOLD || '45s', target: Number(__ENV.SPIKE_TARGET || 80) },
        { duration: __ENV.SPIKE_RAMP_DOWN || '20s', target: 0 },
      ],
      gracefulRampDown: '20s',
      tags: { test_type: 'spike' },
    };
  }

  return {
    thresholds,
    scenarios,
    summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
  };
}
