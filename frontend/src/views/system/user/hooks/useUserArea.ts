import { computed, ref } from 'vue';

import { MessagePlugin } from 'tdesign-vue-next';

import type { SysDictItem } from '@/api/system/dictionary';
import { parseDictValue } from '@/utils/dict';

import type { AreaOption, UserFormModel, UserRow } from '../types';

const toAreaDictValue = (raw: unknown): string | number => {
  if (typeof raw === 'number' && Number.isFinite(raw)) return raw;
  if (typeof raw === 'string') return raw;
  if (typeof raw === 'boolean') return raw ? 'true' : 'false';
  if (raw == null) return '';
  return String(raw);
};

const toNumericId = (value: unknown) => (typeof value === 'number' && Number.isFinite(value) ? value : null);

export const useUserArea = (
  form: UserFormModel,
  areaItems: { value: SysDictItem[] },
  areaDictLoading: { value: boolean },
  areaDictHintMessage: string,
) => {
  const areaOptions = ref<AreaOption[]>([]);
  const areaValue = ref<Array<number | string>>([]);
  const areaLoadingState = ref(false);

  const createDictAreaOptions = (): AreaOption[] => {
    if (areaItems.value.length === 0) return [];
    const districtEntries = areaItems.value
      .map((item) => {
        const row = item as SysDictItem & { province?: string; city?: string; district?: string };
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

  const areaLoading = computed(() => areaLoadingState.value || areaDictLoading.value);

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
    form.provinceId = null;
    form.cityId = null;
    form.districtId = null;
    form.province = '';
    form.city = '';
    form.district = '';
    form.zipCode = '';
  };

  const syncAreaFromUser = async (data: UserRow) => {
    if (!(await loadRootAreas(true))) {
      areaValue.value = [];
      return;
    }
    const province = areaOptions.value.find((item) => item.label === data.province || String(item.value) === data.province);
    const cities = Array.isArray(province?.children) ? province.children : [];
    const city = cities.find((item) => item.label === data.city || String(item.value) === data.city);
    const districts = Array.isArray(city?.children) ? city.children : [];
    const districtFromCity = districts.find((item) => item.label === data.district || String(item.value) === data.district);
    const districtFromProvince = cities.find((item) => item.label === data.district || String(item.value) === data.district);
    const path = (districtFromCity ? [province, city, districtFromCity] : [province, districtFromProvince]).filter(
      Boolean,
    ) as AreaOption[];

    if (path.length > 0) {
      areaValue.value = path.map((item) => item.value);
      form.provinceId = toNumericId(path[0]?.value);
      form.cityId = path.length >= 3 ? toNumericId(path[1]?.value) : null;
      form.districtId = toNumericId(path[path.length - 1]?.value);
      form.province = path[0]?.label || data.province || '';
      form.city = (path.length >= 3 ? path[1]?.label : form.province) || data.city || '';
      form.district = path[path.length - 1]?.label || data.district || '';
    } else if (!data.province && !data.city && !data.district) {
      resetAreaFields();
    }
  };

  const handleAreaChange = (_value: Array<number | string>, context: { node?: { getPath?: () => unknown[] } }) => {
    const node = context?.node;
    if (!node) {
      resetAreaFields();
      return;
    }
    const pathNodes = (node.getPath?.() || []) as Array<{
      value?: unknown;
      label?: string;
      data?: { label?: string; name?: string; zipCode?: string };
    }>;
    if (!pathNodes.length) {
      resetAreaFields();
      return;
    }

    const ids = pathNodes.map((item) => Number(item.value));
    const names = pathNodes.map((item) => String(item.label || item.data?.label || item.data?.name || ''));
    form.provinceId = toNumericId(ids[0]);
    form.province = names[0] ?? '';
    if (pathNodes.length === 2) {
      form.cityId = null;
      form.districtId = toNumericId(ids[1]);
      form.city = form.province;
      form.district = names[1] ?? '';
    } else {
      form.cityId = toNumericId(ids[1]);
      form.districtId = toNumericId(ids[2]);
      form.city = names[1] ?? '';
      form.district = names[2] ?? '';
    }
    form.zipCode = pathNodes[pathNodes.length - 1]?.data?.zipCode || '';
    areaValue.value = pathNodes.map((item) => (item.value ?? '') as number | string);
  };

  return {
    areaOptions,
    areaValue,
    areaLoading,
    loadRootAreas,
    resetAreaFields,
    syncAreaFromUser,
    handleAreaChange,
  };
};
