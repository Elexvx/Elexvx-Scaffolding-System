import { migrateLocalStorageKey } from '@/utils/storage/compat';

export const runSettingMigration = () => {
  if (typeof window !== 'undefined') {
    migrateLocalStorageKey('elexvx.setting', ['setting']);
  }
};
