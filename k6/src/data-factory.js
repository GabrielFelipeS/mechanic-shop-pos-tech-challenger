import { pickOne, randomInt, uniqueSuffix } from './utils.js';

const brands = ['Toyota', 'Honda', 'Volkswagen', 'Chevrolet', 'Hyundai'];
const models = ['Corolla', 'Civic', 'Gol', 'Onix', 'HB20'];
const colors = ['Prata', 'Preto', 'Branco', 'Cinza', 'Azul'];
const complaints = [
  'Veiculo puxa para a direita em alta velocidade.',
  'Ruido metalico ao frear em baixa velocidade.',
  'Consumo de combustivel acima do esperado.',
  'Vibracao no volante acima de 80 km/h.',
];

const diagnoses = [
  'Necessario alinhamento, balanceamento e revisao do conjunto dianteiro.',
  'Pastilhas desgastadas e fluido de freio abaixo do ideal.',
  'Troca de filtros e limpeza do sistema de admissao recomendada.',
  'Desgaste irregular nos pneus com necessidade de ajuste de cambagem.',
];

function onlyDigits(value) {
  return value.replace(/\D/g, '');
}

export function generateCpf() {
  const numbers = Array.from({ length: 9 }, () => randomInt(0, 9));

  const calcDigit = (base, factor) => {
    const total = base.reduce((sum, current) => sum + current * factor--, 0);
    const remainder = (total * 10) % 11;
    return remainder === 10 ? 0 : remainder;
  };

  numbers.push(calcDigit(numbers, 10));
  numbers.push(calcDigit(numbers, 11));

  return onlyDigits(numbers.join(''));
}

export function buildCustomerPayload() {
  const suffix = uniqueSuffix();

  return {
    document: generateCpf(),
    name: `Cliente K6 ${suffix}`,
    email: `cliente.k6.${suffix}@shop.com`,
    role: 'CUSTOMER',
    active: true,
    password: '123456',
    phone: `119${randomInt(10000000, 99999999)}`,
  };
}

export function buildVehiclePayload(ownerId) {
  const suffix = uniqueSuffix();
  const letters = String.fromCharCode(65 + randomInt(0, 25));

  return {
    licensePlate: `${letters}${letters}${letters}${randomInt(0, 9)}${letters}${randomInt(10, 99)}`,
    brand: pickOne(brands),
    model: `${pickOne(models)} ${randomInt(1, 5)}.${randomInt(0, 9)}`,
    year: randomInt(2018, 2026),
    color: pickOne(colors),
    ownerId,
  };
}

export function buildMechanicServicePayload() {
  const suffix = uniqueSuffix();

  return {
    name: `Servico K6 ${suffix}`,
    description: 'Servico criado dinamicamente para validar catalogo e orcamento.',
    estimatedTimeMinutes: randomInt(30, 180),
    price: Number((randomInt(80, 400) + Math.random()).toFixed(2)),
  };
}

export function buildStockItemPayload() {
  const suffix = uniqueSuffix();

  return {
    code: `K6-${suffix}`,
    name: `Peca K6 ${suffix}`,
    type: 'PART',
    description: 'Peca de teste para simular consumo em ordem de servico.',
    quantity: randomInt(8, 20),
    costPrice: Number((randomInt(50, 150) + Math.random()).toFixed(2)),
    salePrice: Number((randomInt(180, 320) + Math.random()).toFixed(2)),
  };
}

export function buildServiceOrderPayload(vehicleExternalId, mechanicExternalId) {
  return {
    vehicleExternalId,
    customerComplaint: pickOne(complaints),
    odometerReading: randomInt(10000, 120000),
    mechanicExternalId,
  };
}

export function buildQuotePayload(partExternalId, mechanicServiceExternalId) {
  return {
    mechanicDiagnosis: pickOne(diagnoses),
    parts: [
      {
        partExternalId,
        quantity: 1,
      },
    ],
    labors: [
      {
        mechanicServiceExternalId,
        quantity: 1,
      },
    ],
  };
}
