import type { App, Directive, DirectiveBinding } from 'vue';

import { hasPerm } from '@/utils/permission';

type PermValue = string | string[];

const PERM_STYLE_KEY = 'permOriginalDisplay';
const PERM_DISABLED_KEY = 'permDisabled';

const normalizeCodes = (value: PermValue): string[] => {
  if (Array.isArray(value)) {
    return value.map((item) => String(item || '').trim()).filter(Boolean);
  }
  const text = String(value || '').trim();
  return text ? [text] : [];
};

const hasAnyPermission = (value: PermValue) => {
  const codes = normalizeCodes(value);
  if (!codes.length) return false;
  return codes.some((code) => hasPerm(code));
};

const restoreHidden = (el: HTMLElement) => {
  const oldDisplay = el.dataset[PERM_STYLE_KEY];
  if (oldDisplay !== undefined) {
    el.style.display = oldDisplay;
    delete el.dataset[PERM_STYLE_KEY];
  }
};

const restoreDisabled = (el: HTMLElement) => {
  if (el.dataset[PERM_DISABLED_KEY] === undefined) return;
  el.removeAttribute('disabled');
  el.removeAttribute('aria-disabled');
  el.style.pointerEvents = '';
  el.style.opacity = '';
  if (el.title && el.title.startsWith('无权限:')) {
    el.title = '';
  }
  delete el.dataset[PERM_DISABLED_KEY];
};

const hideElement = (el: HTMLElement) => {
  if (el.dataset[PERM_STYLE_KEY] === undefined) {
    el.dataset[PERM_STYLE_KEY] = el.style.display || '';
  }
  el.style.display = 'none';
};

const disableElement = (el: HTMLElement, value: PermValue) => {
  const codes = normalizeCodes(value);
  const firstCode = codes[0] || '';
  el.dataset[PERM_DISABLED_KEY] = '1';
  el.setAttribute('disabled', 'true');
  el.setAttribute('aria-disabled', 'true');
  el.style.pointerEvents = 'none';
  el.style.opacity = '0.6';
  if (firstCode) {
    el.title = `无权限: ${firstCode}`;
  }
};

const applyPermission = (el: HTMLElement, binding: DirectiveBinding<PermValue>) => {
  restoreHidden(el);
  restoreDisabled(el);
  const allowed = hasAnyPermission(binding.value);
  if (allowed) return;
  if (binding.modifiers.disable) {
    disableElement(el, binding.value);
    return;
  }
  hideElement(el);
};

const permDirective: Directive<HTMLElement, PermValue> = {
  mounted(el, binding) {
    applyPermission(el, binding);
  },
  updated(el, binding) {
    applyPermission(el, binding);
  },
  unmounted(el) {
    restoreHidden(el);
    restoreDisabled(el);
  },
};

export const installPermDirective = (app: App) => {
  app.directive('perm', permDirective);
};
