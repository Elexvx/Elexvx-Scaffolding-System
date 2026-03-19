import { computed, nextTick, type Ref } from 'vue';

import type { SysDictItem } from '@/api/system/dictionary';
import { resolveLabel } from '@/utils/dict';

import type { UserProfile } from '@/api/user';
import type { CompletionTodoItem, StatusItem } from '../types';
import { sectionTargetMap, sectionWeights, todoConfigs } from '../constants/profileOptions';
import { createDefaultActivePanels } from '../schema/preferencesSchema';
import { hasText } from '../utils/profileGuards';
import { buildFullAddress, maskAccount, maskAddress, maskEmail, maskIdCard, maskPhone } from '../utils/profileMappers';

interface UseUserPreferencesOptions {
  profile: Ref<UserProfile>;
  basicMasked: Ref<boolean>;
  documentMasked: Ref<boolean>;
  securityMasked: Ref<boolean>;
  activePanels: Ref<string[]>;
  isMobile: Ref<boolean>;
  genderItems: Ref<SysDictItem[]>;
  documentTypeItems: Ref<SysDictItem[]>;
  fallbackScroll?: (id: string) => Promise<void> | void;
}

export const useUserPreferences = (options: UseUserPreferencesOptions) => {
  const { profile, basicMasked, documentMasked, securityMasked, activePanels, isMobile, genderItems, documentTypeItems, fallbackScroll } = options;

  const ensurePanelOpen = (panel: string) => {
    if (isMobile.value) {
      activePanels.value = [panel];
      return;
    }
    if (!activePanels.value.includes(panel)) {
      activePanels.value = [...activePanels.value, panel];
    }
  };

  const scrollToBlock = async (key: string) => {
    const targetId = sectionTargetMap[key] || key;
    if (fallbackScroll) {
      await fallbackScroll(targetId);
      return;
    }
    await nextTick();
    const dom = document.getElementById(targetId);
    if (!dom) return;
    dom.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  const focusSection = async (key: 'basic' | 'document' | 'security') => {
    ensurePanelOpen(key);
    await scrollToBlock(key);
  };

  const applyDefaultPanels = (force = false) => {
    if (!force && activePanels.value.length > 0) return;
    activePanels.value = createDefaultActivePanels();
  };

  const fullAddress = computed(() => buildFullAddress(profile.value));

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

  const genderLabel = computed(() => resolveLabel(profile.value.gender, genderItems.value, { unknown: '未知' }));
  const documentTypeLabel = computed(() =>
    resolveLabel(profile.value.idType, documentTypeItems.value, {
      resident_id_card: '居民身份证',
      passport: '护照',
    }),
  );

  const basicStatusItems = computed<StatusItem[]>(() => [
    { key: 'name', label: '姓名', value: profile.value.name || '待补充', done: !isMissing('name') },
    { key: 'gender', label: '性别', value: genderLabel.value || '待补充', done: !isMissing('gender') },
    { key: 'mobile', label: '手机号码', value: displayBasic.value.mobile || '待补充', done: !isMissing('mobile') },
    { key: 'email', label: '电子邮箱', value: displayBasic.value.email || '待补充', done: !isMissing('email') },
    { key: 'address', label: '地址', value: displayBasic.value.address || '待补充', done: !isMissing('address') },
  ]);

  const documentStatusItems = computed<StatusItem[]>(() => [
    { key: 'idType', label: '证件类型', value: documentTypeLabel.value || '待补充', done: !isMissing('idType') },
    { key: 'idCard', label: '证件号码', value: displayDocument.value.idCard || '待补充', done: !isMissing('idCard') },
    { key: 'idValidFrom', label: '证件有效期起', value: profile.value.idValidFrom || '待补充', done: !isMissing('idValidFrom') },
    { key: 'idValidTo', label: '证件有效期止', value: profile.value.idValidTo || '待补充', done: !isMissing('idValidTo') },
  ]);

  const securityStatusItems = computed<StatusItem[]>(() => [
    { key: 'security-mobile', label: '手机验证', value: displaySecurity.value.mobile || '待补充', done: !isMissing('mobile') },
    { key: 'security-email', label: '邮箱验证', value: displaySecurity.value.email || '待补充', done: !isMissing('email') },
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

  const targetSectionKey = computed<'basic' | 'document' | 'security'>(() => {
    const entries: Array<{ key: 'basic' | 'document' | 'security'; missing: number; score: number }> = [
      { key: 'basic', missing: sectionMissingStats.value.basic, score: sectionMissingStats.value.basic * sectionWeights.basic },
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

  const completionTodos = computed<CompletionTodoItem[]>(() => {
    const matched = todoConfigs.filter((item) => isMissing(item.key)).sort((a, b) => b.priority - a.priority);
    return matched.slice(0, 4).map(({ priority, ...item }) => item);
  });

  return {
    displayProfile,
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
  };
};
