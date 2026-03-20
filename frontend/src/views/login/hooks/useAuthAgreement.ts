import { MessagePlugin } from 'tdesign-vue-next';

export const ensureAgreementAccepted = (accepted: boolean, message: string) => {
  if (accepted) return true;
  MessagePlugin.error(message);
  return false;
};
