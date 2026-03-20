<template>
  <div class="login-wrapper">
    <login-header />
    <div class="login-left-container">
      <div class="title-container">
        <h1 class="title margin-no">{{ t('pages.login.loginTitle') }}</h1>
        <h1 class="title">{{ displayName }}</h1>
      </div>

      <login-form-panel v-if="panelType === 'login'" @register="switchType('register')" @forgot="switchType('forgot')" />
      <register-form-panel
        v-else-if="panelType === 'register'"
        @register-success="switchType('login')"
        @back="switchType('login')"
      />
      <forgot-password-panel v-else @back="switchType('login')" @reset-success="switchType('login')" />
      <tdesign-setting />
      <div v-if="copyrightText || icp" class="copyright">
        <div v-if="copyrightText">{{ copyrightText }}</div>
        <div v-if="icp"><a href="https://beian.miit.gov.cn/" target="_blank">{{ icp }}</a></div>
      </div>
    </div>
    <login-hero-panel :bg-style="bgStyle" />
  </div>
</template>

<script setup lang="ts">
import TdesignSetting from '@/layouts/setting.vue';
import { t } from '@/locales';
import LoginHeader from '@/pages/login/components/Header.vue';

import { useLoginPageState } from '../hooks/useLoginPageState';
import ForgotPasswordPanel from './ForgotPasswordPanel.vue';
import LoginFormPanel from './LoginFormPanel.vue';
import LoginHeroPanel from './LoginHeroPanel.vue';
import RegisterFormPanel from './RegisterFormPanel.vue';

const { bgStyle, copyrightText, displayName, icp, panelType, switchType } = useLoginPageState();
</script>

<style scoped lang="less">
@import '../../../pages/login/index.less';
</style>
