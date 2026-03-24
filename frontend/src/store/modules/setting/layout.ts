import type { ModeType } from '@/types/interface';

type LayoutStoreLike = {
  mode: ModeType | 'auto';
  sideMode: ModeType;
  getMediaColor: () => ModeType;
};

export const normalizeLayoutPayload = (store: LayoutStoreLike, payload: Record<string, any>) => {
  const merged = { ...payload };
  const hasMode = merged.mode !== undefined;
  const hasSideMode = merged.sideMode !== undefined;
  const modeChanged = hasMode && merged.mode !== store.mode;
  const sideModeChanged = hasSideMode && merged.sideMode !== store.sideMode;
  const deriveSideMode = (mode: ModeType | 'auto' | undefined) => (mode === 'auto' ? store.getMediaColor() : (mode as ModeType));
  if (modeChanged && !sideModeChanged) {
    merged.sideMode = deriveSideMode(merged.mode);
  } else if (sideModeChanged && !modeChanged) {
    merged.mode = merged.sideMode as ModeType;
  } else if (modeChanged && sideModeChanged) {
    merged.sideMode = deriveSideMode(merged.mode);
  } else if (hasMode && !hasSideMode) {
    merged.sideMode = deriveSideMode(merged.mode);
  } else if (hasSideMode && !hasMode) {
    merged.mode = merged.sideMode as ModeType;
  }
  return merged;
};
