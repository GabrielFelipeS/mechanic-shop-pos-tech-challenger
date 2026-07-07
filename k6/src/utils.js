export function randomBetween(min, max) {
  return Math.random() * (max - min) + min;
}

export function randomInt(min, max) {
  return Math.floor(randomBetween(min, max + 1));
}

export function weightedPick(entries) {
  const total = entries.reduce((sum, entry) => sum + entry.weight, 0);
  let cursor = Math.random() * total;

  for (const entry of entries) {
    cursor -= entry.weight;
    if (cursor <= 0) {
      return entry.value;
    }
  }

  return entries[entries.length - 1].value;
}

export function uniqueSuffix() {
  return `${Date.now()}-${__VU}-${__ITER}-${randomInt(1000, 9999)}`;
}

export function pickOne(values) {
  return values[randomInt(0, values.length - 1)];
}
