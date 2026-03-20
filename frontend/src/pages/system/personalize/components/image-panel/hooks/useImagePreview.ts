import { computed } from 'vue';

import { LOGO_CROP_SPECS } from '../constants/imagePanelOptions';
import type { LogoField } from '../types';

export function useImagePreview(cropField: { value: LogoField | null }) {
  const activeCropSpec = computed(() => (cropField.value ? LOGO_CROP_SPECS[cropField.value] : null));
  const cropDialogTitle = computed(() => (activeCropSpec.value ? `${activeCropSpec.value.label}裁切` : '图片裁切'));
  const cropDialogHint = computed(() =>
    activeCropSpec.value
      ? `尺寸 ${activeCropSpec.value.width}×${activeCropSpec.value.height}，拖拽图片调整裁切区域，滚轮缩放`
      : '',
  );
  const cropFrameStyle = computed(() => ({ width: '100%', height: '400px' }));

  return { activeCropSpec, cropDialogHint, cropDialogTitle, cropFrameStyle };
}
