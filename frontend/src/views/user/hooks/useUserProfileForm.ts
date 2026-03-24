import type { FormInstanceFunctions, SelectOption } from 'tdesign-vue-next';
import { MessagePlugin } from 'tdesign-vue-next';
import { computed, reactive, ref } from 'vue';

import type { UserProfile } from '@/api/user';
import { useDictionary } from '@/hooks/useDictionary';
import { buildDictOptions, parseDictValue } from '@/utils/dict';

import type { AreaOption, ProfileFormModel } from '../types';
import { documentTypeFallbackOptions } from '../constants/profileOptions';
import { createDocumentProfileRules, createProfileFormModel } from '../schema/profileSchema';
import { resolveAreaPathFromProfile, toAreaDictValue, toNumericId } from '../utils/profileMappers';

const areaDictHintMessage = '地址字典未完善，请先在系统字典中完善 address_district 的 province/city/district 字段';

export const useUserProfileForm = () => {
  const basicProfileFormRef = ref<FormInstanceFunctions>();
  const documentProfileFormRef = ref<FormInstanceFunctions>();
  const profileForm = reactive<ProfileFormModel>(createProfileFormModel());

  const genderDict = useDictionary('gender');
  const areaDict = useDictionary('address_district');
  const documentTypeDict = useDictionary('id_document_type');
  const genderOptions = computed<SelectOption[]>(() => buildDictOptions(genderDict.items.value));
  const documentTypeOptions = computed<SelectOption[]>(() =>
    buildDictOptions(documentTypeDict.items.value, documentTypeFallbackOptions),
  );

  const areaOptions = ref<AreaOption[]>([]);
  const areaValue = ref<Array<number | string>>([]);
  const areaLoadingState = ref(false);
  const areaLoading = computed(() => areaLoadingState.value || areaDict.loading.value);

  const createDictAreaOptions = (): AreaOption[] => {
    const areaItems = areaDict.items.value;
    if (areaItems.length === 0) return [];

    const districtEntries = areaItems
      .map((item) => {
        const row = item as unknown as Record<string, unknown>;
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

  const syncAreaFromProfile = async (profile: UserProfile) => {
    if (!(await loadRootAreas(true))) {
      areaValue.value = [];
      return;
    }
    const path = resolveAreaPathFromProfile(profile, areaOptions.value);
    if (path.length > 0) {
      areaValue.value = path.map((item) => item.value);
      profileForm.provinceId = toNumericId(path[0]?.value);
      profileForm.cityId = path.length >= 3 ? toNumericId(path[1]?.value) : null;
      profileForm.districtId = toNumericId(path[path.length - 1]?.value);
      profileForm.province = path[0]?.label || profile.province || '';
      profileForm.city = (path.length >= 3 ? path[1]?.label : profileForm.province) || profile.city || '';
      profileForm.district = path[path.length - 1]?.label || profile.district || '';
    } else if (!profile.province && !profile.city && !profile.district) {
      resetAreaFields();
    }
  };

  const handleAreaChange = (_value: unknown, context: any) => {
    const node = context?.node;
    if (!node) {
      resetAreaFields();
      return;
    }
    const pathNodes = node.getPath?.() || [];
    if (!pathNodes.length) {
      resetAreaFields();
      return;
    }
    const ids = pathNodes.map((item: any) => Number(item.value));
    const names = pathNodes.map((item: any) => String(item.label || ''));
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
    areaValue.value = pathNodes.map((item: any) => item.value as string | number);
  };

  const loadDictionaries = async (force = false) =>
    Promise.all([genderDict.load(force), areaDict.load(force), documentTypeDict.load(force)]);

  return {
    basicProfileFormRef,
    documentProfileFormRef,
    profileForm,
    genderDict,
    documentTypeDict,
    genderOptions,
    documentTypeOptions,
    areaOptions,
    areaValue,
    areaLoading,
    loadDictionaries,
    loadRootAreas,
    resetAreaFields,
    syncAreaFromProfile,
    handleAreaChange,
    documentProfileRules: computed(() => createDocumentProfileRules(profileForm)),
  };
};
