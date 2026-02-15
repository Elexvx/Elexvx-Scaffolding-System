<template>
  <div class="user-page">
    <t-layout class="profile-shell">
      <t-layout class="profile-shell__body">
        <t-content class="profile-shell__content">
          <div class="profile-main" :class="{ 'profile-main--wide': isWideDesktop }">
            <div class="profile-main__left">
              <div id="user-block-profile">
                <ProfileSummaryCard
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
                <CompletionCard
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
                    <InfoSectionCard
                      value="basic"
                      title="基本信息"
                      :percent="basicInfoScore"
                      action-text="编辑信息"
                      :sensitive-toggle="true"
                      :sensitive-visible="!basicMasked"
                      @toggle-sensitive="toggleBasicMasked"
                      @action="openBasicEditDrawer"
                    >
                      <div id="user-block-basic" class="section-body">
                        <FieldStatusItem
                          v-for="item in basicStatusItems"
                          :key="item.key"
                          :label="item.label"
                          :value="item.value"
                          :done="item.done"
                          :action-text="item.done ? '' : '去完善'"
                          @action="openBasicEditDrawer"
                        />
                      </div>
                    </InfoSectionCard>

                    <InfoSectionCard
                      value="document"
                      title="证件信息"
                      :percent="documentInfoScore"
                      action-text="编辑信息"
                      :sensitive-toggle="true"
                      :sensitive-visible="!documentMasked"
                      @toggle-sensitive="toggleDocumentMasked"
                      @action="openDocumentEditDrawer"
                    >
                      <div id="user-block-document" class="section-body">
                        <FieldStatusItem
                          v-for="item in documentStatusItems"
                          :key="item.key"
                          :label="item.label"
                          :value="item.value"
                          :done="item.done"
                          :action-text="item.done ? '' : '去完善'"
                          @action="openDocumentEditDrawer"
                        />
                      </div>
                    </InfoSectionCard>

                    <InfoSectionCard
                      value="security"
                      title="安全设置"
                      :percent="securityInfoScore"
                      :sensitive-toggle="true"
                      :sensitive-visible="!securityMasked"
                      :show-action="false"
                      @toggle-sensitive="toggleSecurityMasked"
                    >
                      <div id="user-block-security" class="section-body">
                        <FieldStatusItem
                          v-for="item in securityStatusItems"
                          :key="item.key"
                          :label="item.label"
                          :value="item.value"
                          :done="item.done"
                          :action-text="item.done ? '' : '去完善'"
                          @action="openBasicEditDrawer"
                        />

                        <div class="section-block-title">修改密码</div>
                        <t-form
                          ref="passwordFormRef"
                          class="password-form"
                          :data="passwordForm"
                          :rules="passwordRules"
                          label-align="right"
                          label-width="120px"
                          @submit="handleSubmitPassword"
                        >
                          <t-form-item label="当前密码" name="oldPassword"
                            ><t-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码"
                          /></t-form-item>
                          <t-form-item label="新密码" name="newPassword"
                            ><t-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码"
                          /></t-form-item>
                          <t-form-item label="确认新密码" name="confirmPassword"
                            ><t-input
                              v-model="passwordForm.confirmPassword"
                              type="password"
                              placeholder="请再次输入新密码"
                          /></t-form-item>
                          <t-form-item class="form-submit" label-width="0"
                            ><t-button theme="primary" type="submit" :loading="changingPassword">修改密码</t-button></t-form-item
                          >
                        </t-form>

                        <div class="section-block-title">最近登录日志</div>
                        <t-table
                          row-key="id"
                          class="login-log-table"
                          :data="loginLogs"
                          :columns="loginLogColumns"
                          :loading="loginLogLoading"
                          :pagination="null"
                        />
                      </div>
                    </InfoSectionCard>
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
          <t-col :xs="24" :sm="12"
            ><t-form-item label="姓名" name="name"><t-input v-model="profileForm.name" placeholder="请输入姓名" /></t-form-item
          ></t-col>
          <t-col :xs="24" :sm="12"
            ><t-form-item label="性别" name="gender"
              ><t-select
                v-model="profileForm.gender"
                :options="genderOptions"
                placeholder="请选择性别"
                clearable /></t-form-item
          ></t-col>
          <t-col :xs="24" :sm="12"
            ><t-form-item label="昵称" name="nickname"
              ><t-input v-model="profileForm.nickname" placeholder="请输入昵称" /></t-form-item
          ></t-col>
          <t-col :xs="24" :sm="12"
            ><t-form-item label="手机号码" name="mobile"
              ><t-input v-model="profileForm.mobile" placeholder="请输入手机号码" /></t-form-item
          ></t-col>
          <t-col :xs="24" :sm="12"
            ><t-form-item label="电子邮箱" name="email"
              ><t-input v-model="profileForm.email" placeholder="请输入邮箱" /></t-form-item
          ></t-col>
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
          <t-col :xs="24" :sm="12"
            ><t-form-item label="邮编" name="zipCode"
              ><t-input v-model="profileForm.zipCode" placeholder="请输入邮编" /></t-form-item
          ></t-col>
          <t-col :xs="24" :sm="12"
            ><t-form-item label="详细地址" name="address"
              ><t-input v-model="profileForm.address" placeholder="请输入详细地址" /></t-form-item
          ></t-col>
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
          <t-col :xs="24" :sm="12"
            ><t-form-item label="证件类型" name="idType"
              ><t-select
                v-model="profileForm.idType"
                :options="documentTypeOptions"
                clearable
                filterable
                placeholder="请选择证件类型" /></t-form-item
          ></t-col>
          <t-col :xs="24" :sm="12"
            ><t-form-item label="证件号码" name="idCard"
              ><t-input v-model="profileForm.idCard" :placeholder="documentNoPlaceholder" /></t-form-item
          ></t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="证件有效期起" name="idValidFrom">
              <t-date-picker
                v-model="profileForm.idValidFrom"
                clearable
                format="YYYY-MM-DD"
                value-type="YYYY-MM-DD"
                style="width: 100%"
              />
            </t-form-item>
          </t-col>
          <t-col :xs="24" :sm="12">
            <t-form-item label="证件有效期止" name="idValidTo">
              <t-date-picker
                v-model="profileForm.idValidTo"
                clearable
                format="YYYY-MM-DD"
                value-type="YYYY-MM-DD"
                style="width: 100%"
              />
            </t-form-item>
          </t-col>
        </t-row>
      </t-form>
      <template #footer>
        <t-space class="tdesign-starter-action-bar">
          <t-button variant="outline" @click="documentEditVisible = false">取消</t-button>
          <t-button theme="primary" :loading="updatingProfile" @click="documentProfileFormRef?.submit()">保存</t-button>
        </t-space>
      </template>
    </confirm-drawer>
  </div>
</template>

<script setup lang="ts">
import type {
  FormInstanceFunctions,
  FormRule,
  PrimaryTableCol,
  SelectOption,
  SubmitContext,
} from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue';

import type { ChangePasswordRequest, UserProfile, UserProfileUpdate } from '@/api/user';
import { changePassword, getMyProfile, updateMyProfile } from '@/api/user';
import ConfirmDrawer from '@/components/ConfirmDrawer.vue';
import { useDictionary } from '@/hooks/useDictionary';
import { useUserStore } from '@/store';
import { buildDictOptions, parseDictValue, resolveLabel } from '@/utils/dict';
import { request } from '@/utils/request';

import CompletionCard from './components/CompletionCard.vue';
import FieldStatusItem from './components/FieldStatusItem.vue';
import InfoSectionCard from './components/InfoSectionCard.vue';
import ProfileSummaryCard from './components/ProfileSummaryCard.vue';

interface AreaOption {
  label: string;
  value: number | string;
  level?: number;
  zipCode?: string | null;
  children?: AreaOption[] | boolean;
}

interface LoginLogRow {
  id: number;
  detail?: string;
  account?: string;
  ipAddress?: string;
  deviceInfo?: string;
  createdAt?: string;
}

interface PageResult<T> {
  list: T[];
  total: number;
}

interface CompletionTodoItem {
  key: string;
  title: string;
  gain: string;
  section: string;
  actionText?: string;
}

interface CompletionTodoConfig extends CompletionTodoItem {
  priority: number;
}

const MOBILE_BREAKPOINT = 768;
const DESKTOP_BREAKPOINT = 1200;

const DOC_TYPE_RESIDENT_ID_CARD = 'resident_id_card';
const DOC_TYPE_PASSPORT = 'passport';
const RESIDENT_ID_CARD_WEIGHTS = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2];
const RESIDENT_ID_CARD_CHECKSUM_CODES = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'];
const documentTypeFallbackOptions: SelectOption[] = [
  { label: '居民身份证', value: DOC_TYPE_RESIDENT_ID_CARD },
  { label: '护照', value: DOC_TYPE_PASSPORT },
];

const userStore = useUserStore();

const viewportWidth = ref(typeof window === 'undefined' ? DESKTOP_BREAKPOINT : window.innerWidth);
const isMobile = computed(() => viewportWidth.value < MOBILE_BREAKPOINT);
const isWideDesktop = computed(() => viewportWidth.value > DESKTOP_BREAKPOINT);

const profileLoading = ref(false);
const updatingProfile = ref(false);
const uploadHeaders = computed(() => ({ Authorization: userStore.token }));
const profile = ref<UserProfile>({} as UserProfile);

const basicMasked = ref(true);
const documentMasked = ref(true);
const securityMasked = ref(true);

const basicEditVisible = ref(false);
const documentEditVisible = ref(false);
const drawerSize = computed(() => (isMobile.value ? '100%' : '760px'));

const sectionTargetMap: Record<string, string> = {
  basic: 'user-block-basic',
  document: 'user-block-document',
  security: 'user-block-security',
};

const scrollToBlock = async (key: string) => {
  const targetId = sectionTargetMap[key] || key;
  await nextTick();
  const dom = document.getElementById(targetId);
  if (!dom) return;
  dom.scrollIntoView({ behavior: 'smooth', block: 'start' });
};

const activePanels = ref<string[]>([]);
const ensurePanelOpen = (panel: string) => {
  if (isMobile.value) {
    activePanels.value = [panel];
    return;
  }
  if (!activePanels.value.includes(panel)) {
    activePanels.value = [...activePanels.value, panel];
  }
};

const focusSection = async (key: 'basic' | 'document' | 'security') => {
  ensurePanelOpen(key);
  await scrollToBlock(key);
};

const maskMiddle = (value: string, prefixKeep: number, suffixKeep: number, maskChar = '*') => {
  const text = (value || '').trim();
  if (!text) return '';
  if (text.includes('*')) return text;
  const keepHead = Math.max(0, prefixKeep);
  const keepTail = Math.max(0, suffixKeep);
  if (text.length <= keepHead + keepTail) return maskChar.repeat(Math.max(4, text.length));
  const maskedLen = Math.max(4, text.length - keepHead - keepTail);
  return `${text.slice(0, keepHead)}${maskChar.repeat(maskedLen)}${text.slice(text.length - keepTail)}`;
};

const maskPhone = (value?: string) => {
  const text = (value || '').trim();
  if (!text) return '';
  const digits = text.replace(/\D/g, '');
  if (digits.length === 11) return maskMiddle(digits, 3, 4);
  return maskMiddle(text, 3, 2);
};

const maskEmail = (value?: string) => {
  const text = (value || '').trim();
  if (!text) return '';
  if (text.includes('*')) return text;
  const at = text.indexOf('@');
  if (at <= 1) return text;
  return `${text.slice(0, 1)}****${text.slice(at)}`;
};

const maskIdCard = (value?: string) => {
  const text = (value || '').trim();
  if (!text) return '';
  if (text.includes('*')) return text;
  if (text.length >= 8) return maskMiddle(text, 4, 4);
  return maskMiddle(text, 1, 1);
};

const maskAddress = (value?: string) => {
  const text = (value || '').trim();
  if (!text) return '';
  if (text.includes('*')) return text;
  if (text.length <= 6) return `${text[0]}****`;
  return `${text.slice(0, 6)}****`;
};

const maskAccount = (value?: string) => {
  const text = (value || '').trim();
  if (!text) return '';
  if (text.includes('*')) return text;
  if (text.length <= 4) return maskMiddle(text, 1, 0);
  return maskMiddle(text, 2, 2);
};

const toggleBasicMasked = () => {
  basicMasked.value = !basicMasked.value;
};

const toggleDocumentMasked = () => {
  documentMasked.value = !documentMasked.value;
};

const toggleSecurityMasked = () => {
  securityMasked.value = !securityMasked.value;
};

const basicProfileFormRef = ref<FormInstanceFunctions>();
const documentProfileFormRef = ref<FormInstanceFunctions>();
const profileForm = reactive({
  name: '',
  nickname: '',
  gender: '',
  mobile: '',
  email: '',
  idType: '',
  idCard: '',
  idValidFrom: '',
  idValidTo: '',
  province: '',
  city: '',
  district: '',
  provinceId: null as number | null,
  cityId: null as number | null,
  districtId: null as number | null,
  zipCode: '',
  address: '',
  tags: '',
});

const genderDict = useDictionary('gender');
const areaDict = useDictionary('address_district');
const documentTypeDict = useDictionary('id_document_type');
const genderOptions = computed(() => buildDictOptions(genderDict.items.value));
const documentTypeOptions = computed(() => buildDictOptions(documentTypeDict.items.value, documentTypeFallbackOptions));

const areaOptions = ref<AreaOption[]>([]);
const areaValue = ref<Array<number | string>>([]);
const areaLoadingState = ref(false);
const areaDictHintMessage = '地址字典未完善，请先在系统字典中完善 address_district 的 province/city/district 字段';
const areaLoading = computed(() => areaLoadingState.value || areaDict.loading.value);

const normalizeDocumentType = (value?: string) => {
  if (!value) return '';
  const raw = String(value).trim();
  if (!raw) return '';
  const lower = raw.toLowerCase();
  if (['resident_id_card', 'id_card', 'identity_card', 'china_id_card'].includes(lower) || raw === '居民身份证') {
    return DOC_TYPE_RESIDENT_ID_CARD;
  }
  if (lower === 'passport' || raw === '护照') {
    return DOC_TYPE_PASSPORT;
  }
  return lower;
};

const isValidResidentIdCard = (value: string) => {
  const text = value.trim().toUpperCase();
  if (!/^[1-9]\d{16}[0-9X]$/.test(text)) return false;
  const birth = text.slice(6, 14);
  if (!/^\d{8}$/.test(birth)) return false;
  const y = Number(birth.slice(0, 4));
  const m = Number(birth.slice(4, 6));
  const d = Number(birth.slice(6, 8));
  const date = new Date(y, m - 1, d);
  if (date.getFullYear() !== y || date.getMonth() !== m - 1 || date.getDate() !== d) return false;

  let sum = 0;
  for (let i = 0; i < 17; i += 1) sum += Number(text[i]) * RESIDENT_ID_CARD_WEIGHTS[i];
  return RESIDENT_ID_CARD_CHECKSUM_CODES[sum % 11] === text[17];
};

const isValidPassport = (value: string) => /^[A-Z0-9]{5,17}$/.test(value.trim().toUpperCase());
const validateDocumentNumber = (docType?: string, docNo?: string) => {
  const number = (docNo || '').trim();
  if (!number) return true;
  const type = normalizeDocumentType(docType);
  if (!type) return false;
  if (type === DOC_TYPE_RESIDENT_ID_CARD) return isValidResidentIdCard(number);
  if (type === DOC_TYPE_PASSPORT) return isValidPassport(number);
  return false;
};
const validateDocumentDateRange = (from?: string, to?: string) => !from || !to || from <= to;

const documentNoPlaceholder = computed(() => {
  const type = normalizeDocumentType(profileForm.idType);
  if (type === DOC_TYPE_RESIDENT_ID_CARD) return '请输入18位居民身份证号码';
  if (type === DOC_TYPE_PASSPORT) return '请输入护照号码（5-17位字母数字）';
  return '请先选择证件类型，再输入证件号码';
});

const toAreaDictValue = (raw: unknown): string | number => {
  if (typeof raw === 'number' && Number.isFinite(raw)) return raw;
  if (typeof raw === 'string') return raw;
  if (typeof raw === 'boolean') return raw ? 'true' : 'false';
  if (raw == null) return '';
  return String(raw);
};

const createDictAreaOptions = (): AreaOption[] => {
  const areaItems = areaDict.items.value;
  if (areaItems.length === 0) return [];

  const districtEntries = areaItems
    .map((item) => {
      const row = item as any;
      const province = String(row.province || '').trim();
      const city = String(row.city || '').trim();
      const district = String(row.district || '').trim();
      return { label: item.label, value: toAreaDictValue(item.id ?? parseDictValue(item)), province, city, district };
    })
    .filter((entry) => !!entry.province && !!entry.city && !!entry.district);

  const buildDistrictOptions = (province?: string, city?: string, level = 3): AreaOption[] => {
    if (!province || !city) return [];
    const unique = new Set<string>();
    return districtEntries
      .filter((entry) => entry.province === province && entry.city === city)
      .filter((entry) => {
        const key = `${province}/${city}/${entry.district}`;
        if (unique.has(key)) return false;
        unique.add(key);
        return true;
      })
      .map((entry) => ({ label: entry.district, value: entry.value, level, children: [] }));
  };

  const provinceSet = Array.from(new Set(districtEntries.map((entry) => entry.province)));
  return provinceSet.map((province) => {
    const provinceEntries = districtEntries.filter((entry) => entry.province === province);
    const cities = Array.from(new Set(provinceEntries.map((entry) => entry.city))).filter(Boolean);
    const normalCities = cities.filter((city) => city !== province);
    const municipalityDistricts = buildDistrictOptions(province, province, 2);
    const cityNodes: AreaOption[] = normalCities.map((city) => ({
      label: city,
      value: `${province}/${city}`,
      level: 2,
      children: buildDistrictOptions(province, city, 3),
    }));

    return {
      label: province,
      value: toAreaDictValue(province),
      level: 1,
      children: [...municipalityDistricts, ...cityNodes],
    };
  });
};

const areaDictReady = computed(() => {
  const options = createDictAreaOptions();
  if (options.length === 0) return false;
  const hasSecondLevel = options.some((province) => Array.isArray(province.children) && province.children.length > 0);
  const hasDistrict = options.some(
    (province) =>
      Array.isArray(province.children) &&
      province.children.some((child) => {
        if (!Array.isArray(child.children)) return false;
        if (child.children.length === 0) return true;
        return child.children.length > 0;
      }),
  );
  return hasSecondLevel && hasDistrict;
});

const ensureAreaDictReady = (notify = true) => {
  if (areaDictReady.value) return true;
  areaOptions.value = [];
  if (notify) MessagePlugin.error(areaDictHintMessage);
  return false;
};

const loadRootAreas = async (notify = true) => {
  areaLoadingState.value = true;
  try {
    if (!ensureAreaDictReady(notify)) return false;
    areaOptions.value = createDictAreaOptions();
    return true;
  } finally {
    areaLoadingState.value = false;
  }
};

const resetAreaFields = () => {
  areaValue.value = [];
  profileForm.provinceId = null;
  profileForm.cityId = null;
  profileForm.districtId = null;
  profileForm.province = '';
  profileForm.city = '';
  profileForm.district = '';
  profileForm.zipCode = '';
};

const toNumericId = (value: unknown) => (typeof value === 'number' && Number.isFinite(value) ? value : null);

const syncAreaFromProfile = async (data: UserProfile) => {
  if (!(await loadRootAreas(true))) {
    areaValue.value = [];
    return;
  }
  const province = areaOptions.value.find(
    (item) => item.label === data.province || String(item.value) === data.province,
  );
  const cities = Array.isArray(province?.children) ? province.children : [];
  const city = cities.find((item) => item.label === data.city || String(item.value) === data.city);
  const districts = Array.isArray(city?.children) ? city.children : [];
  const districtFromCity = districts.find(
    (item) => item.label === data.district || String(item.value) === data.district,
  );
  const districtFromProvince = cities.find((item) => item.label === data.district || String(item.value) === data.district);
  const path = (districtFromCity ? [province, city, districtFromCity] : [province, districtFromProvince]).filter(
    Boolean,
  ) as AreaOption[];

  if (path.length > 0) {
    areaValue.value = path.map((item) => item.value);
    profileForm.provinceId = toNumericId(path[0]?.value);
    profileForm.cityId = path.length >= 3 ? toNumericId(path[1]?.value) : null;
    profileForm.districtId = toNumericId(path[path.length - 1]?.value);
    profileForm.province = path[0]?.label || data.province || '';
    profileForm.city = (path.length >= 3 ? path[1]?.label : profileForm.province) || data.city || '';
    profileForm.district = path[path.length - 1]?.label || data.district || '';
  } else if (!data.province && !data.city && !data.district) {
    resetAreaFields();
  }
};

const handleAreaChange = (_value: any, context: any) => {
  const node = context?.node;
  if (!node) return resetAreaFields();
  const pathNodes = node.getPath?.() || [];
  if (!pathNodes.length) return resetAreaFields();

  const ids = pathNodes.map((item: any) => Number(item.value));
  const names = pathNodes.map((item: any) => String(item.label || item.data?.label || item.data?.name || ''));
  profileForm.provinceId = toNumericId(ids[0]);
  profileForm.province = names[0] ?? '';
  if (pathNodes.length === 2) {
    profileForm.cityId = null;
    profileForm.districtId = toNumericId(ids[1]);
    profileForm.city = profileForm.province;
    profileForm.district = names[1] ?? '';
  } else {
    profileForm.cityId = toNumericId(ids[1]);
    profileForm.districtId = toNumericId(ids[2]);
    profileForm.city = names[1] ?? '';
    profileForm.district = names[2] ?? '';
  }
  profileForm.zipCode = pathNodes[pathNodes.length - 1]?.data?.zipCode || '';
  areaValue.value = pathNodes.map((item: any) => item.value);
};

const basicProfileRules: Record<string, FormRule[]> = {
  name: [{ required: true, message: '请输入姓名', type: 'error' }],
  email: [{ email: true, message: '请输入正确的邮箱地址', type: 'warning' }],
};

const documentProfileRules: Record<string, FormRule[]> = {
  idType: [
    {
      validator: (val: string) => !profileForm.idCard?.trim() || Boolean(normalizeDocumentType(val)),
      message: '已填写证件号码时，请先选择证件类型',
      type: 'error',
    },
  ],
  idCard: [
    {
      validator: (val: string) => validateDocumentNumber(profileForm.idType, val),
      message: '证件号码格式与证件类型不匹配',
      type: 'error',
    },
  ],
  idValidTo: [
    {
      validator: (val: string) => validateDocumentDateRange(profileForm.idValidFrom, val),
      message: '证件有效期止不能早于证件有效期起',
      type: 'error',
    },
  ],
};

const changingPassword = ref(false);
const passwordFormRef = ref<FormInstanceFunctions>();
const passwordForm = reactive<ChangePasswordRequest>({ oldPassword: '', newPassword: '', confirmPassword: '' });
const passwordRules: Record<string, FormRule[]> = {
  oldPassword: [{ required: true, message: '请输入当前密码', type: 'error' }],
  newPassword: [
    { required: true, message: '请输入新密码', type: 'error' },
    { min: 6, max: 20, message: '密码长度应为6-20位', type: 'error' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', type: 'error' },
    { validator: (val: string) => val === passwordForm.newPassword, message: '两次输入的密码不一致', type: 'error' },
  ],
};

const loginLogs = ref<LoginLogRow[]>([]);
const loginLogLoading = ref(false);
const loginLogColumns: PrimaryTableCol[] = [
  { colKey: 'createdAt', title: '登录时间', width: 180 },
  { colKey: 'ipAddress', title: 'IP', width: 140 },
  { colKey: 'deviceInfo', title: '设备信息', minWidth: 220, ellipsis: true },
  { colKey: 'detail', title: '备注', minWidth: 200, ellipsis: true },
];

const hasText = (value?: string) => Boolean(value && value.trim());

const displayedRoles = computed(() =>
  profile.value?.roles?.length ? profile.value.roles : userStore.userInfo?.roles || [],
);

const roleTag = computed(() => {
  const admin = displayedRoles.value.find((item) => String(item || '').toLowerCase() === 'admin');
  return admin || displayedRoles.value[0] || 'user';
});

const fullAddress = computed(() => {
  const province = (profile.value.province || '').trim();
  const city = (profile.value.city || '').trim();
  const district = (profile.value.district || '').trim();
  const address = (profile.value.address || '').trim();
  const parts = [province, city, district, address].filter(Boolean);
  return parts.filter((part, idx) => idx === 0 || part !== parts[idx - 1]).join('');
});

const displayBasic = computed(() => {
  if (!basicMasked.value) {
    return {
      mobile: (profile.value.mobile || '').trim(),
      email: (profile.value.email || '').trim(),
      address: fullAddress.value,
    };
  }
  return {
    mobile: maskPhone(profile.value.mobile),
    email: maskEmail(profile.value.email),
    address: maskAddress(fullAddress.value),
  };
});

const displayProfile = computed(() => ({
  account: maskAccount(profile.value.account),
  mobile: maskPhone(profile.value.mobile),
  email: maskEmail(profile.value.email),
  address: maskAddress(fullAddress.value),
}));

const displaySecurity = computed(() => {
  if (!securityMasked.value) {
    return {
      mobile: (profile.value.mobile || '').trim(),
      email: (profile.value.email || '').trim(),
    };
  }
  return {
    mobile: maskPhone(profile.value.mobile),
    email: maskEmail(profile.value.email),
  };
});

const displayDocument = computed(() => {
  if (!documentMasked.value) {
    return {
      idCard: (profile.value.idCard || '').trim(),
    };
  }
  return {
    idCard: maskIdCard(profile.value.idCard),
  };
});

const profileContacts = computed(() => {
  const contacts = [
    `账号：${displayProfile.value.account || '-'}`,
    `邮箱：${displayProfile.value.email || '待补充'}`,
    `手机：${displayProfile.value.mobile || '待补充'}`,
  ];
  if (displayProfile.value.address) contacts.push(`地址：${displayProfile.value.address}`);
  return contacts;
});

const fallbackCompleteness = computed(() => {
  const missing = new Set<string>();
  if (!hasText(profile.value.name)) missing.add('name');
  if (!hasText(profile.value.gender)) missing.add('gender');
  if (!hasText(profile.value.mobile)) missing.add('mobile');
  if (!hasText(profile.value.email)) missing.add('email');
  const hasAddress = [profile.value.province, profile.value.city, profile.value.district, profile.value.address]
    .map((item) => (item || '').trim())
    .some(Boolean);
  if (!hasAddress) missing.add('address');
  if (!hasText(profile.value.idType)) missing.add('idType');
  if (!hasText(profile.value.idCard)) missing.add('idCard');
  if (!hasText(profile.value.idValidFrom)) missing.add('idValidFrom');
  if (!hasText(profile.value.idValidTo)) missing.add('idValidTo');

  const basicComplete = 5 - ['name', 'gender', 'mobile', 'email', 'address'].filter((key) => missing.has(key)).length;
  const documentComplete = 4 - ['idType', 'idCard', 'idValidFrom', 'idValidTo'].filter((key) => missing.has(key)).length;
  return {
    missing,
    basicScore: Math.round((basicComplete * 100) / 5),
    documentScore: Math.round((documentComplete * 100) / 4),
    score: Math.round(((basicComplete + documentComplete) * 100) / 9),
  };
});

const completenessScore = computed(() =>
  typeof profile.value.completenessScore === 'number' ? profile.value.completenessScore : fallbackCompleteness.value.score,
);
const basicInfoScore = computed(() =>
  typeof profile.value.basicInfoScore === 'number' ? profile.value.basicInfoScore : fallbackCompleteness.value.basicScore,
);
const documentInfoScore = computed(() =>
  typeof profile.value.documentInfoScore === 'number'
    ? profile.value.documentInfoScore
    : fallbackCompleteness.value.documentScore,
);
const incompleteItems = computed(() =>
  Array.isArray(profile.value.incompleteItems) && profile.value.incompleteItems.length > 0
    ? profile.value.incompleteItems
    : Array.from(fallbackCompleteness.value.missing),
);

const incompleteSet = computed(() => new Set(incompleteItems.value.map((item) => String(item))));
const isMissing = (key: string) => incompleteSet.value.has(key);

const genderLabel = computed(() => resolveLabel(profile.value.gender, genderDict.items.value, { unknown: '未知' }));
const documentTypeLabel = computed(() =>
  resolveLabel(profile.value.idType, documentTypeDict.items.value, {
    resident_id_card: '居民身份证',
    passport: '护照',
  }),
);

const basicStatusItems = computed(() => [
  { key: 'name', label: '姓名', value: profile.value.name || '待补充', done: !isMissing('name') },
  { key: 'gender', label: '性别', value: genderLabel.value || '待补充', done: !isMissing('gender') },
  { key: 'mobile', label: '手机号码', value: displayBasic.value.mobile || '待补充', done: !isMissing('mobile') },
  { key: 'email', label: '电子邮箱', value: displayBasic.value.email || '待补充', done: !isMissing('email') },
  { key: 'address', label: '地址', value: displayBasic.value.address || '待补充', done: !isMissing('address') },
]);

const documentStatusItems = computed(() => [
  { key: 'idType', label: '证件类型', value: documentTypeLabel.value || '待补充', done: !isMissing('idType') },
  { key: 'idCard', label: '证件号码', value: displayDocument.value.idCard || '待补充', done: !isMissing('idCard') },
  { key: 'idValidFrom', label: '证件有效期起', value: profile.value.idValidFrom || '待补充', done: !isMissing('idValidFrom') },
  { key: 'idValidTo', label: '证件有效期止', value: profile.value.idValidTo || '待补充', done: !isMissing('idValidTo') },
]);

const securityStatusItems = computed(() => [
  {
    key: 'security-mobile',
    label: '手机验证',
    value: displaySecurity.value.mobile || '待补充',
    done: !isMissing('mobile'),
  },
  {
    key: 'security-email',
    label: '邮箱验证',
    value: displaySecurity.value.email || '待补充',
    done: !isMissing('email'),
  },
]);

const securityInfoScore = computed(() => {
  const done = securityStatusItems.value.filter((item) => item.done).length;
  return Math.round((done * 100) / Math.max(1, securityStatusItems.value.length));
});

const sectionMissingStats = computed(() => {
  const basic = ['name', 'gender', 'mobile', 'email', 'address'].filter((key) => isMissing(key)).length;
  const document = ['idType', 'idCard', 'idValidFrom', 'idValidTo'].filter((key) => isMissing(key)).length;
  const security = ['mobile', 'email'].filter((key) => isMissing(key)).length;
  return { basic, document, security };
});

const sectionWeights: Record<'basic' | 'document' | 'security', number> = {
  basic: 1,
  document: 3,
  security: 2,
};

const targetSectionKey = computed<'basic' | 'document' | 'security'>(() => {
  const entries: Array<{ key: 'basic' | 'document' | 'security'; missing: number; score: number }> = [
    {
      key: 'basic',
      missing: sectionMissingStats.value.basic,
      score: sectionMissingStats.value.basic * sectionWeights.basic,
    },
    {
      key: 'document',
      missing: sectionMissingStats.value.document,
      score: sectionMissingStats.value.document * sectionWeights.document,
    },
    {
      key: 'security',
      missing: sectionMissingStats.value.security,
      score: sectionMissingStats.value.security * sectionWeights.security,
    },
  ];

  entries.sort((a, b) => b.score - a.score || b.missing - a.missing);
  return entries[0]?.key || 'basic';
});

const todoConfigs: CompletionTodoConfig[] = [
  { key: 'idCard', title: '完善证件号码', gain: '+15%', section: 'document', actionText: '去填写', priority: 100 },
  { key: 'idType', title: '选择证件类型', gain: '+12%', section: 'document', actionText: '去填写', priority: 95 },
  { key: 'idValidFrom', title: '补全证件有效期起', gain: '+10%', section: 'document', actionText: '去填写', priority: 90 },
  { key: 'idValidTo', title: '补全证件有效期止', gain: '+10%', section: 'document', actionText: '去填写', priority: 88 },
  { key: 'mobile', title: '补全手机号码', gain: '+8%', section: 'security', actionText: '去填写', priority: 80 },
  { key: 'email', title: '补全电子邮箱', gain: '+8%', section: 'security', actionText: '去填写', priority: 78 },
  { key: 'address', title: '补全联系地址', gain: '+6%', section: 'basic', actionText: '去填写', priority: 60 },
  { key: 'name', title: '补全姓名信息', gain: '+5%', section: 'basic', actionText: '去填写', priority: 55 },
  { key: 'gender', title: '补全性别信息', gain: '+4%', section: 'basic', actionText: '去填写', priority: 50 },
];

const completionTodos = computed(() => {
  const matched = todoConfigs.filter((item) => isMissing(item.key)).sort((a, b) => b.priority - a.priority);
  return matched.slice(0, 4).map(({ priority, ...item }) => item);
});

const handleTodoClick = async (item: CompletionTodoItem) => {
  const allowedSections = new Set(['basic', 'document', 'security']);
  const section = (allowedSections.has(item.section) ? item.section : targetSectionKey.value) as
    | 'basic'
    | 'document'
    | 'security';
  await focusSection(section);
  if (section === 'basic' || section === 'security') {
    await openBasicEditDrawer();
  } else if (section === 'document') {
    await openDocumentEditDrawer();
  }
};

const handleCompletionCta = async () => {
  await focusSection(targetSectionKey.value);
};

const loadDictionaries = async (force = false) =>
  Promise.all([genderDict.load(force), areaDict.load(force), documentTypeDict.load(force)]);

const normalizeGender = (value?: string) =>
  value === 'secret' && genderDict.items.value.some((item) => item.value === 'unknown') ? 'unknown' : value || '';

const applyDefaultPanels = (force = false) => {
  if (!force && activePanels.value.length > 0) return;
  activePanels.value = ['basic'];
};

watch(
  () => isMobile.value,
  () => {
    applyDefaultPanels();
  },
);

const fetchLoginLogs = async (account?: string) => {
  loginLogLoading.value = true;
  try {
    const res = await request.get<PageResult<LoginLogRow>>({
      url: '/system/log/page',
      params: { action: 'LOGIN', keyword: account || undefined, page: 0, size: 5 },
    });
    loginLogs.value = res.list || [];
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
    const res = await getMyProfile();
    profile.value = res;
    userStore.userInfo = { ...userStore.userInfo, name: res.name || '', avatar: res.avatar || '' };
    Object.assign(profileForm, {
      name: res.name || '',
      nickname: res.nickname || '',
      gender: normalizeGender(res.gender),
      mobile: res.mobile || '',
      email: res.email || '',
      idType: normalizeDocumentType(res.idType),
      idCard: res.idCard || '',
      idValidFrom: res.idValidFrom || '',
      idValidTo: res.idValidTo || '',
      seat: res.seat || '',
      provinceId: res.provinceId ?? null,
      province: res.province || '',
      cityId: res.cityId ?? null,
      city: res.city || '',
      districtId: res.districtId ?? null,
      district: res.district || '',
      zipCode: res.zipCode || '',
      address: res.address || '',
      tags: res.tags || '',
    });
    await syncAreaFromProfile(res);
    applyDefaultPanels(true);
  } catch {
    MessagePlugin.error('加载个人信息失败');
  } finally {
    fetchLoginLogs(profile.value.account || profile.value.email);
    profileLoading.value = false;
  }
};

const openBasicEditDrawer = async () => {
  basicEditVisible.value = true;
  try {
    await loadDictionaries(true);
    areaOptions.value = [];
    await loadRootAreas();
  } catch (error) {
    console.error('Load area dictionaries failed:', error);
  }
};

const openDocumentEditDrawer = async () => {
  documentEditVisible.value = true;
  try {
    await loadDictionaries(true);
  } catch (error) {
    console.error('Load dictionaries failed:', error);
  }
};

const handleUpdateBasicProfile = async (context: SubmitContext) => {
  if (context.validateResult !== true) return;

  const payload: UserProfileUpdate = {
    name: profileForm.name?.trim() || '',
    nickname: profileForm.nickname?.trim() || '',
    gender: profileForm.gender || '',
    mobile: profileForm.mobile?.trim() || '',
    email: profileForm.email?.trim() || '',
    provinceId: profileForm.provinceId,
    province: profileForm.province || '',
    cityId: profileForm.cityId,
    city: profileForm.city || '',
    districtId: profileForm.districtId,
    district: profileForm.district || '',
    zipCode: profileForm.zipCode?.trim() || '',
    address: profileForm.address?.trim() || '',
  };

  updatingProfile.value = true;
  try {
    await updateMyProfile(payload);
    MessagePlugin.success('个人资料更新成功');
    basicEditVisible.value = false;
    await fetchProfile();
  } catch (error: any) {
    const raw = String(error?.message || '个人资料更新失败');
    const humanMsg = raw.replace(/\s*\[\d{3}\]\s*$/, '').trim();
    MessagePlugin.error(humanMsg || '个人资料更新失败');
  } finally {
    updatingProfile.value = false;
  }
};

const handleUpdateDocumentProfile = async (context: SubmitContext) => {
  if (context.validateResult !== true) return;

  const payload: UserProfileUpdate = {
    idType: normalizeDocumentType(profileForm.idType) || '',
    idCard: profileForm.idCard?.trim() || '',
    idValidFrom: profileForm.idValidFrom || undefined,
    idValidTo: profileForm.idValidTo || undefined,
  };

  updatingProfile.value = true;
  try {
    await updateMyProfile(payload);
    MessagePlugin.success('证件信息更新成功');
    documentEditVisible.value = false;
    await fetchProfile();
  } catch (error: any) {
    const raw = String(error?.message || '证件信息更新失败');
    const humanMsg = raw.replace(/\s*\[\d{3}\]\s*$/, '').trim();
    MessagePlugin.error(humanMsg || '证件信息更新失败');
  } finally {
    updatingProfile.value = false;
  }
};

const handleSubmitPassword = async (context: SubmitContext) => {
  if (context.validateResult !== true) return;
  changingPassword.value = true;
  try {
    await changePassword(passwordForm);
    MessagePlugin.success('密码修改成功');
    passwordForm.oldPassword = '';
    passwordForm.newPassword = '';
    passwordForm.confirmPassword = '';
    passwordFormRef.value?.reset();
  } catch (err: any) {
    const raw = String(err?.message || '密码修改失败');
    const humanMsg = raw.replace(/\s*\[\d{3}\]\s*$/, '').trim();
    MessagePlugin.error(humanMsg || '密码修改失败');
  } finally {
    changingPassword.value = false;
  }
};

const handleAvatarSuccess = async (context: any) => {
  const url = context.response?.data?.url;
  if (!url) return;
  try {
    await updateMyProfile({ avatar: url });
    profile.value.avatar = url;
    userStore.userInfo.avatar = url;
    MessagePlugin.success('头像更新成功');
  } catch {
    MessagePlugin.error('头像更新失败');
  }
};

const handleAvatarFail = (context: any) => {
  console.error('Avatar upload failed:', context);
  const msg = context.response?.data?.message || context.response?.statusText || '上传失败';
  MessagePlugin.error(`头像上传失败: ${msg}`);
};

const updateViewport = () => {
  if (typeof window === 'undefined') return;
  viewportWidth.value = window.innerWidth;
};

onMounted(() => {
  fetchProfile();
  updateViewport();
  if (typeof window !== 'undefined') window.addEventListener('resize', updateViewport);
});

onUnmounted(() => {
  if (typeof window !== 'undefined') window.removeEventListener('resize', updateViewport);
});
</script>

<style lang="less" scoped>
.user-page {
  --user-page-gap: 16px;

  .profile-shell {
    background: transparent;
  }

  .profile-shell__body {
    background: transparent;
  }

  .profile-shell__content {
    overflow: visible;
  }

  .profile-main {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: var(--user-page-gap);
  }

  .profile-main--wide {
    grid-template-columns: 340px minmax(0, 1fr);
    align-items: start;
  }

  .profile-main__left,
  .profile-main__right {
    display: grid;
    gap: var(--user-page-gap);
  }

  .sections-shell {
    display: grid;
    grid-template-columns: minmax(0, 1fr);
    gap: var(--user-page-gap);
  }

  .info-sections {
    border: 1px solid var(--td-border-level-1-color);
    border-radius: var(--td-radius-large);
    background: var(--td-bg-color-container);
    overflow: hidden;

    :deep(.t-collapse-panel) {
      margin: 0;
      border: none;
      border-bottom: 1px solid var(--td-border-level-1-color);
      border-radius: 0;
      background: transparent;
      padding: 0;
    }

    :deep(.t-collapse-panel:last-child) {
      border-bottom: none;
    }

    :deep(.t-collapse-panel__header) {
      min-height: 72px;
      padding: 0 20px;
      border-bottom: none;
      background: var(--td-bg-color-container);
    }

    :deep(.t-collapse-panel__body) {
      border-top: 1px solid var(--td-border-level-1-color);
      background: var(--td-bg-color-container);
      overflow: hidden;
    }

    :deep(.t-collapse-panel__body--collapsed) {
      display: none;
    }

    :deep(.t-collapse-panel__content) {
      padding: 0 20px 20px;
    }
  }

  .section-body {
    padding-top: 10px;
  }

  .section-block-title {
    margin-top: 12px;
    margin-bottom: 8px;
    font-size: 14px;
    font-weight: 600;
    color: var(--td-text-color-primary);
  }

  .password-form {
    :deep(.t-form__controls) {
      min-width: 0;
      flex: 1;
    }

    :deep(.t-input) {
      width: 100%;
    }
  }

  .form-submit {
    :deep(.t-form__controls-content) {
      display: flex;
      justify-content: flex-end;
    }
  }

  .login-log-table {
    margin-top: 4px;
  }

  @media (max-width: 1200px) {
    .profile-main--wide {
      grid-template-columns: minmax(0, 1fr);
    }
  }
  @media (max-width: 1200px) {
    .summary-grid {
      grid-template-columns: 1fr;
    }
  }

  @media (max-width: 900px) {
    .info-sections {
      :deep(.t-collapse-panel) {
        padding: 0;
      }

      :deep(.t-collapse-panel__header) {
        min-height: 64px;
        padding: 0 14px;
      }

      :deep(.t-collapse-panel__content) {
        padding: 0 14px 14px;
      }
    }

    .login-log-table {
      :deep(.t-table__content) {
        overflow-x: auto;
      }
    }
  }
}
</style>
