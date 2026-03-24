<template>
  <div class="user-page">
    <t-layout class="profile-shell">
      <t-layout class="profile-shell__body">
        <t-content class="profile-shell__content">
          <div class="profile-main" :class="{ 'profile-main--wide': isWideDesktop }">
            <div class="profile-main__left">
              <div id="user-block-profile">
                <profile-summary-card
                  :loading="profileLoading"
                  :avatar="profile.avatar"
                  :name="profile.name"
                  :role-tag="roleTag"
                  :contacts="profileContacts"
                  :upload-headers="uploadHeaders"
                  @avatar-success="handleAvatarSuccess"
                  @avatar-fail="handleAvatarFail"
                />
              </div>

              <div id="user-block-completion">
                <profile-preferences-card
                  :loading="profileLoading"
                  :percent="completenessScore"
                  :todos="completionTodos"
                  cta-text="一键去补全"
                  @item-click="handleTodoClick"
                  @cta-click="handleCompletionCta"
                />
              </div>
            </div>

            <div class="profile-main__right">
              <div class="sections-shell">
                <div class="sections-detail">
                  <t-collapse v-model="activePanels" class="info-sections">
                    <profile-base-info-card
                      :percent="basicInfoScore"
                      :masked="basicMasked"
                      :items="basicStatusItems"
                      @toggle-sensitive="basicMasked = !basicMasked"
                      @edit="openBasicEditDrawer"
                    />
                    <profile-document-info-card
                      :percent="documentInfoScore"
                      :masked="documentMasked"
                      :items="documentStatusItems"
                      @toggle-sensitive="documentMasked = !documentMasked"
                      @edit="openDocumentEditDrawer"
                    />
                    <profile-security-card
                      :percent="securityInfoScore"
                      :masked="securityMasked"
                      :items="securityStatusItems"
                      :login-logs="loginLogs"
                      :login-log-loading="loginLogLoading"
                      :login-log-columns="loginLogColumns"
                      @toggle-sensitive="securityMasked = !securityMasked"
                      @edit-basic="openBasicEditDrawer"
                      @open-password-dialog="passwordDialogVisible = true"
                    />
                  </t-collapse>
                </div>
              </div>
            </div>
          </div>
        </t-content>
      </t-layout>
    </t-layout>

    <confirm-drawer v-model:visible="basicEditVisible" header="编辑基本信息" :size="drawerSize">
      <t-form
        ref="basicProfileFormRef"
        class="drawer-form--single"
        :data="profileForm"
        :rules="basicProfileRules"
        label-align="right"
        label-width="120px"
        layout="vertical"
        @submit="handleUpdateBasicProfile"
      >
        <t-row :gutter="[24, 24]">
          <t-col :xs="24" :sm="12"><t-form-item label="姓名" name="name"><t-input v-model="profileForm.name" placeholder="请输入姓名" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="性别" name="gender"><t-select v-model="profileForm.gender" :options="genderOptions" placeholder="请选择性别" clearable /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="昵称" name="nickname"><t-input v-model="profileForm.nickname" placeholder="请输入昵称" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="手机号码" name="mobile"><t-input v-model="profileForm.mobile" placeholder="请输入手机号码" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="电子邮箱" name="email"><t-input v-model="profileForm.email" placeholder="请输入邮箱" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="省/市/区县" name="provinceId">
              <t-cascader
                v-model="areaValue"
                :options="areaOptions"
                :loading="areaLoading"
                value-type="full"
                :show-all-levels="true"
                clearable
                placeholder="请选择省/市/区县"
                @change="handleAreaChange"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="邮编" name="zipCode"><t-input v-model="profileForm.zipCode" placeholder="请输入邮编" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="详细地址" name="address"><t-input v-model="profileForm.address" placeholder="请输入详细地址" /></t-form-item></t-col>
        </t-row>
      </t-form>
      <template #footer>
        <t-space class="tdesign-starter-action-bar">
          <t-button variant="outline" @click="basicEditVisible = false">取消</t-button>
          <t-button theme="primary" :loading="updatingProfile" @click="basicProfileFormRef?.submit()">保存</t-button>
        </t-space>
      </template>
    </confirm-drawer>

    <confirm-drawer v-model:visible="documentEditVisible" header="编辑证件信息" :size="drawerSize">
      <t-form
        ref="documentProfileFormRef"
        class="drawer-form--single"
        :data="profileForm"
        :rules="documentProfileRules"
        label-align="right"
        label-width="120px"
        layout="vertical"
        @submit="handleUpdateDocumentProfile"
      >
        <t-row :gutter="[24, 24]">
          <t-col :xs="24" :sm="12">
            <t-form-item label="证件类型" name="idType">
              <t-select v-model="profileForm.idType" :options="documentTypeOptions" clearable filterable placeholder="请选择证件类型" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="证件号码" name="idCard">
              <t-input v-model="profileForm.idCard" :placeholder="documentNoPlaceholder" />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="证件有效期起" name="idValidFrom"><t-date-picker v-model="profileForm.idValidFrom" clearable format="YYYY-MM-DD" value-type="YYYY-MM-DD" style="width: 100%" /></t-form-item></t-col>
          <t-col :xs="24" :sm="12"><t-form-item label="证件有效期止" name="idValidTo"><t-date-picker v-model="profileForm.idValidTo" clearable format="YYYY-MM-DD" value-type="YYYY-MM-DD" style="width: 100%" /></t-form-item></t-col>
        </t-row>
      </t-form>
      <template #footer>
        <t-space class="tdesign-starter-action-bar">
          <t-button variant="outline" @click="documentEditVisible = false">取消</t-button>
          <t-button theme="primary" :loading="updatingProfile" @click="documentProfileFormRef?.submit()">保存</t-button>
        </t-space>
      </template>
    </confirm-drawer>

    <password-dialog
      v-model:visible="passwordDialogVisible"
      :loading="changingPassword"
      :password-form="passwordForm"
      :password-rules="passwordRules"
      @submit="submitPassword"
    />
  </div>
</template>

<script setup lang="ts">
import type { SubmitContext } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';

import type { UserProfile } from '@/api/user';
import { getMyProfile, updateMyProfile } from '@/api/user';
import ConfirmDrawer from '@/components/ConfirmDrawer.vue';
import { useUserStore } from '@/store';
import { resolveLabel } from '@/utils/dict';
import { request } from '@/utils/request';

import { loginLogColumns } from '../constants/profileOptions';
import { useAvatarUpload } from '../hooks/useAvatarUpload';
import { usePasswordDialog } from '../hooks/usePasswordDialog';
import { useUserPreferences } from '../hooks/useUserPreferences';
import { useUserProfileForm } from '../hooks/useUserProfileForm';
import { useUserProfileState } from '../hooks/useUserProfileState';
import { basicProfileRules } from '../schema/profileSchema';
import type { LoginLogRow, PageResult } from '../types';
import { normalizeDocumentType } from '../utils/profileGuards';
import { createBasicProfilePayload, createDocumentProfilePayload, normalizeGender, resolveDocumentNoPlaceholder } from '../utils/profileMappers';
import PasswordDialog from './PasswordDialog.vue';
import ProfileBaseInfoCard from './ProfileBaseInfoCard.vue';
import ProfileDocumentInfoCard from './ProfileDocumentInfoCard.vue';
import ProfilePreferencesCard from './ProfilePreferencesCard.vue';
import ProfileSecurityCard from './ProfileSecurityCard.vue';
import ProfileSummaryCard from './ProfileSummaryCard.vue';

const userStore = useUserStore();
const profile = ref<UserProfile>({} as UserProfile);
const loginLogs = ref<LoginLogRow[]>([]);
const {
  profileLoading,
  updatingProfile,
  loginLogLoading,
  basicEditVisible,
  documentEditVisible,
  passwordDialogVisible,
  activePanels,
  basicMasked,
  documentMasked,
  securityMasked,
  isMobile,
  isWideDesktop,
  drawerSize,
  updateViewport,
} = useUserProfileState();
const { basicProfileFormRef, documentProfileFormRef, profileForm, genderDict, documentTypeDict, genderOptions, documentTypeOptions, areaOptions, areaValue, areaLoading, loadDictionaries, loadRootAreas, syncAreaFromProfile, handleAreaChange, documentProfileRules } =
  useUserProfileForm();
const { changingPassword, passwordForm, passwordRules, submitPassword } = usePasswordDialog();
const { uploadHeaders, handleAvatarSuccess, handleAvatarFail } = useAvatarUpload(profile);

const {
  profileContacts,
  completenessScore,
  basicInfoScore,
  documentInfoScore,
  securityInfoScore,
  basicStatusItems,
  documentStatusItems,
  securityStatusItems,
  completionTodos,
  targetSectionKey,
  applyDefaultPanels,
  focusSection,
} = useUserPreferences({
  profile,
  basicMasked,
  documentMasked,
  securityMasked,
  activePanels,
  isMobile,
  genderItems: computed(() => genderDict.items.value),
  documentTypeItems: computed(() => documentTypeDict.items.value),
});

const documentNoPlaceholder = computed(() => resolveDocumentNoPlaceholder(profileForm.idType));
const roleTag = computed(() => {
  const roles = profile.value?.roles?.length ? profile.value.roles : userStore.userInfo?.roles || [];
  const admin = roles.find((item) => String(item || '').toLowerCase() === 'admin');
  return admin || roles[0] || 'user';
});

watch(
  () => isMobile.value,
  () => {
    applyDefaultPanels();
  },
);

const fetchLoginLogs = async (account?: string) => {
  loginLogLoading.value = true;
  try {
    const response = await request.get<PageResult<LoginLogRow>>({
      url: '/system/log/page',
      params: { action: 'LOGIN', keyword: account || undefined, page: 0, size: 5 },
    });
    loginLogs.value = response.list || [];
  } catch {
    MessagePlugin.error('加载登录日志失败');
  } finally {
    loginLogLoading.value = false;
  }
};

const fetchProfile = async () => {
  profileLoading.value = true;
  try {
    await loadDictionaries();
    const response = await getMyProfile();
    profile.value = response;
    userStore.userInfo = { ...userStore.userInfo, name: response.name || '', avatar: response.avatar || '' };
    applyDefaultPanels(true);
    const normalized = normalizeGender(response.gender, genderDict.items.value.some((item) => item.value === 'unknown'));
    Object.assign(profileForm, {
      ...profileForm,
      name: response.name || '',
      nickname: response.nickname || '',
      gender: normalized,
      mobile: response.mobile || '',
      email: response.email || '',
      idType: normalizeDocumentType(response.idType),
      idCard: response.idCard || '',
      idValidFrom: response.idValidFrom || '',
      idValidTo: response.idValidTo || '',
      provinceId: response.provinceId ?? null,
      province: response.province || '',
      cityId: response.cityId ?? null,
      city: response.city || '',
      districtId: response.districtId ?? null,
      district: response.district || '',
      zipCode: response.zipCode || '',
      address: response.address || '',
      tags: response.tags || '',
    });
    await syncAreaFromProfile(response);
  } catch {
    MessagePlugin.error('加载个人信息失败');
  } finally {
    await fetchLoginLogs(profile.value.account || profile.value.email);
    profileLoading.value = false;
  }
};

const openBasicEditDrawer = async () => {
  basicEditVisible.value = true;
  await loadDictionaries(true);
  areaOptions.value = [];
  await loadRootAreas();
};

const openDocumentEditDrawer = async () => {
  documentEditVisible.value = true;
  await loadDictionaries(true);
};

const handleUpdateBasicProfile = async (context: SubmitContext) => {
  if (context.validateResult !== true) return;
  updatingProfile.value = true;
  try {
    await updateMyProfile(createBasicProfilePayload(profileForm));
    MessagePlugin.success('个人资料更新成功');
    basicEditVisible.value = false;
    await fetchProfile();
  } catch (error: unknown) {
    const raw = String((error as { message?: string })?.message || '个人资料更新失败');
    MessagePlugin.error(raw.replace(/\s*\[\d{3}\]\s*$/, '').trim() || '个人资料更新失败');
  } finally {
    updatingProfile.value = false;
  }
};

const handleUpdateDocumentProfile = async (context: SubmitContext) => {
  if (context.validateResult !== true) return;
  updatingProfile.value = true;
  try {
    await updateMyProfile(createDocumentProfilePayload(profileForm));
    MessagePlugin.success('证件信息更新成功');
    documentEditVisible.value = false;
    await fetchProfile();
  } catch (error: unknown) {
    const raw = String((error as { message?: string })?.message || '证件信息更新失败');
    MessagePlugin.error(raw.replace(/\s*\[\d{3}\]\s*$/, '').trim() || '证件信息更新失败');
  } finally {
    updatingProfile.value = false;
  }
};

const handleTodoClick = async (item: { section: string }) => {
  const allowedSections = new Set(['basic', 'document', 'security']);
  const section = (allowedSections.has(item.section) ? item.section : targetSectionKey.value) as 'basic' | 'document' | 'security';
  await focusSection(section);
  if (section === 'document') {
    await openDocumentEditDrawer();
    return;
  }
  await openBasicEditDrawer();
};

const handleCompletionCta = async () => {
  await focusSection(targetSectionKey.value);
};

onMounted(() => {
  void fetchProfile();
  updateViewport();
  if (typeof window !== 'undefined') window.addEventListener('resize', updateViewport);
});

onUnmounted(() => {
  if (typeof window !== 'undefined') window.removeEventListener('resize', updateViewport);
});
</script>
<style scoped lang="less" src="./UserProfilePageMain.less"></style>
