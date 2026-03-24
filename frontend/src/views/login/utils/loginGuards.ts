import type { LoginMethod } from '../types';

export const hasSequentialChars = (value: string) => {
  if (!value || value.length < 3) return false;
  let streak = 1;
  let prev = -1;
  for (const raw of value) {
    if (!/[a-z0-9]/i.test(raw)) {
      streak = 1;
      prev = -1;
      continue;
    }
    const current = raw.toLowerCase().charCodeAt(0);
    if (prev !== -1 && Math.abs(current - prev) === 1) {
      streak += 1;
      if (streak >= 3) return true;
    } else {
      streak = 1;
    }
    prev = current;
  }
  return false;
};

export const resolveAvailableLoginMethod = (
  current: LoginMethod,
  smsEnabled: boolean,
  emailEnabled: boolean,
): LoginMethod => {
  if (current === 'phone' && !smsEnabled) {
    return emailEnabled ? 'email' : 'password';
  }
  if (current === 'email' && !emailEnabled) {
    return smsEnabled ? 'phone' : 'password';
  }
  return current;
};
