export const normalizeNumber = (value: unknown, fallback: number) => (typeof value === 'number' && Number.isFinite(value) ? value : fallback);
export const normalizeBoolean = (value: unknown, fallback: boolean) => (typeof value === 'boolean' ? value : fallback);
