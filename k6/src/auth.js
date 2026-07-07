import { getData, request } from './http.js';
import { config } from './config.js';

function login(email, password, role) {
  const { payload } = request('POST', '/api/auth/login', {
    body: { email, password },
    expectedStatus: 200,
    tags: { endpoint: 'auth_login', role },
  });

  return payload.data;
}

export function authenticateSeedUsers() {
  return {
    adminToken: login(config.credentials.admin.email, config.credentials.admin.password, 'admin'),
    receptionistToken: login(
      config.credentials.receptionist.email,
      config.credentials.receptionist.password,
      'receptionist'
    ),
    mechanicToken: login(config.credentials.mechanic.email, config.credentials.mechanic.password, 'mechanic'),
    warehouseToken: login(
      config.credentials.warehouse.email,
      config.credentials.warehouse.password,
      'warehouse'
    ),
  };
}

export function resolveMechanicId(receptionistToken) {
  const data = getData('GET', '/api/users/search?role=MECHANIC&size=1', {
    token: receptionistToken,
    expectedStatus: 200,
    tags: { endpoint: 'users_search_mechanic' },
  });

  const firstMechanic = data?.content?.[0];
  if (!firstMechanic?.externalId) {
    throw new Error('No mechanic user found to assign service orders.');
  }

  return firstMechanic.externalId;
}

export function loginCustomer(email, password) {
  return login(email, password, 'customer');
}
