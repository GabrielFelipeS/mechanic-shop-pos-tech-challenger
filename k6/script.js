import { sleep } from 'k6';
import exec from 'k6/execution';
import { buildOptions } from './src/config.js';
import { authenticateSeedUsers, resolveMechanicId } from './src/auth.js';
import { runCatalogJourney, runServiceOrderJourney } from './src/workflows.js';
import { randomBetween, weightedPick } from './src/utils.js';

export const options = buildOptions();

export function setup() {
  const auth = authenticateSeedUsers();
  const mechanicId = resolveMechanicId(auth.receptionistToken);

  return {
    ...auth,
    mechanicId,
  };
}

function runMixedWorkload(context, scenarioName) {
  const selectedFlow = weightedPick([
    { value: 'catalog', weight: 65 },
    { value: 'serviceOrder', weight: 35 },
  ]);

  if (selectedFlow === 'catalog') {
    runCatalogJourney(context, scenarioName);
  } else {
    runServiceOrderJourney(context, scenarioName);
  }

  sleep(randomBetween(1, 3));
}

export function baseline(data) {
  runMixedWorkload(data, exec.scenario.name || 'baseline');
}

export function stress(data) {
  runMixedWorkload(data, exec.scenario.name || 'stress');
}

export function spike(data) {
  runMixedWorkload(data, exec.scenario.name || 'spike');
}
