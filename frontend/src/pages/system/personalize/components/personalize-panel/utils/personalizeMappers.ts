export const normalizePath = (parentPath: string, path?: string) => {
  const next = String(path || '').trim();
  if (!next) return parentPath || '';
  if (next.startsWith('/')) return next;
  if (!parentPath) return `/${next}`;
  return `${parentPath.replace(/\/$/, '')}/${next}`;
};

export const createPersonalizePayload = (form: Record<string, any>, pathMap: Record<string, string>) => ({
  allowMultiDeviceLogin: form.allowMultiDeviceLogin,
  logRetentionDays: form.logRetentionDays,
  defaultHome: pathMap[form.defaultHome] || normalizePath('', form.defaultHome),
  autoTheme: form.autoTheme,
  headerGithubUrl: form.headerGithubUrl,
  headerHelpUrl: form.headerHelpUrl,
  maintenanceEnabled: form.maintenanceEnabled,
  maintenanceMessage: form.maintenanceMessage,
  lightStartTime: form.lightStartTime,
  darkStartTime: form.darkStartTime,
});
