import { downloadDictTemplate, exportDictItems } from '@/api/system/dictionary';
import { exportOperationLogsCsv } from '@/api/system/log';
import { downloadModulePackage } from '@/api/system/module';
import { downloadSensitiveWordsTemplate } from '@/api/system/sensitive';
import { downloadBlobResponse, resolveApiActionUrl } from '@/utils/importExport';

export const importExportApi = {
  dict: {
    importItemsAction: (dictId: number) => resolveApiActionUrl(`/system/dict/${dictId}/items/import`),
    exportItems: (dictId: number) => exportDictItems(dictId),
    downloadTemplate: () => downloadDictTemplate(),
  },
  sensitive: {
    importWordsAction: () => resolveApiActionUrl('/system/sensitive/words/import'),
    downloadTemplate: () => downloadSensitiveWordsTemplate(),
  },
  log: {
    exportCsv: (params?: Parameters<typeof exportOperationLogsCsv>[0]) => exportOperationLogsCsv(params),
  },
  modules: {
    installPackageAction: () => resolveApiActionUrl('/system/modules/package/install'),
    downloadPackage: (moduleKey: string) => downloadModulePackage(moduleKey),
  },
  utils: {
    downloadBlobResponse,
    resolveApiActionUrl,
  },
};
