export const useVerificationPolicyForm = (form: any, disableConfirmVisible: any) => {
  const handleCaptchaEnabledChange = (value: string | number | boolean) => {
    if (!value) {
      form.value.captchaEnabled = true;
      disableConfirmVisible.value = true;
    }
  };
  const confirmDisableCaptcha = () => {
    form.value.captchaEnabled = false;
    disableConfirmVisible.value = false;
  };
  const cancelDisableCaptcha = () => {
    disableConfirmVisible.value = false;
  };
  return { handleCaptchaEnabledChange, confirmDisableCaptcha, cancelDisableCaptcha };
};
