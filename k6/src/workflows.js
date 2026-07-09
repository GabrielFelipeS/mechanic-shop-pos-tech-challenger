import { group } from 'k6';
import { buildCustomerPayload, buildMechanicServicePayload, buildQuotePayload, buildServiceOrderPayload, buildStockItemPayload, buildVehiclePayload } from './data-factory.js';
import { loginCustomer } from './auth.js';
import { getData, request } from './http.js';
import { pickOne } from './utils.js';

function createCustomer(receptionistToken, scenarioName) {
  const customerPayload = buildCustomerPayload();
  const customerId = getData('POST', '/api/users/create', {
    token: receptionistToken,
    body: customerPayload,
    expectedStatus: 201,
    tags: { flow: 'customer_create', scenario: scenarioName },
  });

  return {
    customerId,
    customerPayload,
  };
}

function createVehicle(receptionistToken, ownerId, scenarioName) {
  const vehiclePayload = buildVehiclePayload(ownerId);
  return getData('POST', '/api/vehicles/create', {
    token: receptionistToken,
    body: vehiclePayload,
    expectedStatus: 201,
    tags: { flow: 'vehicle_create', scenario: scenarioName },
  });
}

function createMechanicService(mechanicToken, scenarioName) {
  const servicePayload = buildMechanicServicePayload();
  return getData('POST', '/api/mechanic-services/create', {
    token: mechanicToken,
    body: servicePayload,
    expectedStatus: 201,
    tags: { flow: 'mechanic_service_create', scenario: scenarioName },
  });
}

function createStockItem(warehouseToken, scenarioName) {
  const stockPayload = buildStockItemPayload();
  return getData('POST', '/api/stock-items/create', {
    token: warehouseToken,
    body: stockPayload,
    expectedStatus: 201,
    tags: { flow: 'stock_item_create', scenario: scenarioName },
  });
}

function createServiceOrder(receptionistToken, vehicleExternalId, mechanicExternalId, scenarioName) {
  const orderPayload = buildServiceOrderPayload(vehicleExternalId, mechanicExternalId);
  return getData('POST', '/api/service-orders/create', {
    token: receptionistToken,
    body: orderPayload,
    expectedStatus: 201,
    tags: { flow: 'service_order_create', scenario: scenarioName },
  });
}

function updateQuote(mechanicToken, serviceOrderId, stockItemId, mechanicServiceId, scenarioName) {
  return getData('PUT', `/api/service-orders/${serviceOrderId}/quote`, {
    token: mechanicToken,
    body: buildQuotePayload(stockItemId, mechanicServiceId),
    expectedStatus: 200,
    tags: { flow: 'service_order_quote', scenario: scenarioName },
  });
}

function approveBudget(customerToken, serviceOrderId, scenarioName) {
  return getData('POST', `/api/service-orders/${serviceOrderId}/budget-response`, {
    token: customerToken,
    body: { approved: true },
    expectedStatus: 200,
    tags: { flow: 'service_order_budget_approve', scenario: scenarioName },
  });
}

function requestApproval(mechanicToken, serviceOrderId, scenarioName) {
  return getData('POST', `/api/service-orders/${serviceOrderId}/request-approval`, {
    token: mechanicToken,
    expectedStatus: 200,
    tags: { flow: 'service_order_request_approval', scenario: scenarioName },
  });
}

function finishService(mechanicToken, serviceOrderId, scenarioName) {
  return getData('POST', `/api/service-orders/${serviceOrderId}/finish`, {
    token: mechanicToken,
    expectedStatus: 200,
    tags: { flow: 'service_order_finish', scenario: scenarioName },
  });
}

function deliverVehicle(receptionistToken, serviceOrderId, scenarioName) {
  return getData('POST', `/api/service-orders/${serviceOrderId}/deliver`, {
    token: receptionistToken,
    expectedStatus: 200,
    tags: { flow: 'service_order_deliver', scenario: scenarioName },
  });
}

function searchUsers(receptionistToken, scenarioName) {
  getData('GET', '/api/users/search?size=5', {
    token: receptionistToken,
    expectedStatus: 200,
    tags: { flow: 'users_search', scenario: scenarioName },
  });
}

function searchVehicles(token, scenarioName) {
  getData('GET', '/api/vehicles/search?size=5', {
    token,
    expectedStatus: 200,
    tags: { flow: 'vehicles_search', scenario: scenarioName },
  });
}

function searchMechanicServices(token, scenarioName) {
  getData('GET', '/api/mechanic-services/search?size=5', {
    token,
    expectedStatus: 200,
    tags: { flow: 'mechanic_services_search', scenario: scenarioName },
  });
}

function searchStockItems(token, scenarioName) {
  getData('GET', '/api/stock-items/search?size=5', {
    token,
    expectedStatus: 200,
    tags: { flow: 'stock_items_search', scenario: scenarioName },
  });
}

function searchServiceOrders(token, scenarioName) {
  getData('GET', '/api/service-orders/search?size=5', {
    token,
    expectedStatus: 200,
    tags: { flow: 'service_orders_search', scenario: scenarioName },
  });
}

export function runCatalogJourney(context, scenarioName) {
  group(`catalog-journey:${scenarioName}`, () => {
    searchUsers(context.receptionistToken, scenarioName);
    searchVehicles(pickOne([context.receptionistToken, context.mechanicToken]), scenarioName);
    searchMechanicServices(pickOne([context.receptionistToken, context.mechanicToken]), scenarioName);
    searchStockItems(pickOne([context.receptionistToken, context.warehouseToken, context.mechanicToken]), scenarioName);
    searchServiceOrders(pickOne([context.receptionistToken, context.mechanicToken]), scenarioName);
  });
}

export function runServiceOrderJourney(context, scenarioName) {
  group(`service-order-journey:${scenarioName}`, () => {
    const { customerId, customerPayload } = createCustomer(context.receptionistToken, scenarioName);
    const vehicleId = createVehicle(context.receptionistToken, customerId, scenarioName);
    const mechanicServiceId = createMechanicService(context.mechanicToken, scenarioName);
    const stockItemId = createStockItem(context.warehouseToken, scenarioName);
    const serviceOrderId = createServiceOrder(
      context.receptionistToken,
      vehicleId,
      context.mechanicId,
      scenarioName
    );

    updateQuote(context.mechanicToken, serviceOrderId, stockItemId, mechanicServiceId, scenarioName);
    requestApproval(context.mechanicToken, serviceOrderId, scenarioName);

    const customerToken = loginCustomer(customerPayload.email, customerPayload.password);
    approveBudget(customerToken, serviceOrderId, scenarioName);

    finishService(context.mechanicToken, serviceOrderId, scenarioName);
    deliverVehicle(context.receptionistToken, serviceOrderId, scenarioName);

    request('GET', `/api/service-orders/${serviceOrderId}`, {
      token: customerToken,
      expectedStatus: 200,
      tags: { flow: 'service_order_get_by_id', scenario: scenarioName },
    });
  });
}
