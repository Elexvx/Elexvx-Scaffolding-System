import { Color } from 'tvision-color';

import type { ModeType } from '@/types/interface';
import { generateColorMap, insertThemeStylesheet } from '@/utils/color';

import { DARK_CHART_COLORS, LIGHT_CHART_COLORS } from '@/config/color';

type ThemeStoreLike = {
  autoTheme: boolean;
  autoThemeTimer: any;
  lightStartTime: string;
  darkStartTime: string;
  mode: ModeType | 'auto';
  sideMode: ModeType;
  colorList: Record<string, any>;
  chartColors: string[];
  displayMode: ModeType;
  getMediaColor: () => ModeType;
  updateConfig: (payload: Record<string, any>) => void;
};

export const initAutoTheme = (store: ThemeStoreLike) => {
  if (store.autoThemeTimer) {
    clearInterval(store.autoThemeTimer);
    store.autoThemeTimer = null;
  }
  if (!store.autoTheme) return;
  checkAutoTheme(store);
  store.autoThemeTimer = setInterval(() => {
    checkAutoTheme(store);
  }, 60000);
};

export const checkAutoTheme = (store: ThemeStoreLike) => {
  if (!store.autoTheme) {
    if (store.autoThemeTimer) {
      clearInterval(store.autoThemeTimer);
      store.autoThemeTimer = null;
    }
    return;
  }
  const now = new Date();
  const currentMinutes = now.getHours() * 60 + now.getMinutes();
  const parseTime = (timeStr: string) => {
    const [hours, minutes] = timeStr.split(':').map(Number);
    return hours * 60 + minutes;
  };
  const lightStart = parseTime(store.lightStartTime || '06:00');
  const darkStart = parseTime(store.darkStartTime || '18:00');
  let targetMode: ModeType = 'light';
  if (lightStart < darkStart) {
    targetMode = currentMinutes >= lightStart && currentMinutes < darkStart ? 'light' : 'dark';
  } else {
    targetMode = currentMinutes >= lightStart || currentMinutes < darkStart ? 'light' : 'dark';
  }
  const currentAttr = document.documentElement.getAttribute('theme-mode');
  const isDomDark = currentAttr === 'dark';
  const isTargetDark = targetMode === 'dark';
  if (store.mode !== targetMode || isDomDark !== isTargetDark) {
    store.updateConfig({ mode: targetMode });
  }
};

export const changeMode = (mode: ModeType | 'auto') => {
  let theme = mode;
  if (mode === 'auto') {
    theme = getMediaColor();
  }
  const isDarkMode = theme === 'dark';
  if (isDarkMode) {
    document.documentElement.setAttribute('theme-mode', 'dark');
  } else {
    document.documentElement.removeAttribute('theme-mode');
  }
  return isDarkMode ? DARK_CHART_COLORS : LIGHT_CHART_COLORS;
};

export const changeSideMode = (mode: ModeType) => {
  if (mode === 'dark') {
    document.documentElement.setAttribute('side-mode', 'dark');
  } else {
    document.documentElement.removeAttribute('side-mode');
  }
};

export const getMediaColor = (): ModeType => {
  const media = window.matchMedia('(prefers-color-scheme:dark)');
  return media.matches ? 'dark' : 'light';
};

export const changeBrandTheme = (store: ThemeStoreLike, brandTheme: string) => {
  const mode = store.displayMode;
  const colorKey = `${brandTheme}[${mode}]`;
  let colorMap = store.colorList[colorKey];
  if (colorMap === undefined) {
    const [{ colors: newPalette, primary: brandColorIndex }] = Color.getColorGradations({
      colors: [brandTheme],
      step: 10,
      remainInput: false,
    });
    colorMap = generateColorMap(brandTheme, newPalette, mode, brandColorIndex);
    store.colorList[colorKey] = colorMap;
  }
  insertThemeStylesheet(brandTheme, colorMap, mode);
  document.documentElement.setAttribute('theme-color', brandTheme);
};
