<template>
  <div class="drag-captcha">
    <div class="verify-img-out" :style="{ height: `${height + vSpace}px` }">
      <div class="verify-img-panel" :style="{ width: `${width}px`, height: `${height}px` }">
        <img v-if="backImgBase" :src="`data:image/png;base64,${backImgBase}`" alt="" />
        <div v-show="showRefresh" class="verify-refresh" @click="refresh">
          <i class="iconfont icon-refresh"></i>
        </div>
        <transition name="tips">
          <span v-if="tipWords" class="verify-tips" :class="passFlag ? 'suc-bg' : 'err-bg'">
            {{ tipWords }}
          </span>
        </transition>
      </div>
    </div>

    <div
      ref="barAreaRef"
      class="verify-bar-area"
      :style="{ width: `${width}px`, height: `${barHeight}px`, lineHeight: `${barHeight}px` }"
    >
      <span class="verify-msg">{{ text }}</span>
      <div
        class="verify-left-bar"
        :style="{
          width: leftBarWidth !== null ? `${leftBarWidth}px` : `${barHeight}px`,
          height: `${barHeight}px`,
          borderColor: leftBarBorderColor,
          transition: transitionWidth,
        }"
      >
        <span class="verify-msg">{{ finishText }}</span>
        <div
          class="verify-move-block"
          :style="{
            width: `${barHeight}px`,
            height: `${barHeight}px`,
            backgroundColor: moveBlockBackgroundColor,
            left: `${moveBlockLeft}px`,
            transition: transitionLeft,
          }"
          @touchstart.prevent="start"
          @mousedown.prevent="start"
        >
          <i class="verify-icon iconfont" :class="iconClass" :style="{ color: iconColor }"></i>
          <div
            class="verify-sub-block"
            :style="{
              width: `${blockWidth}px`,
              height: `${height}px`,
              top: `-${height + vSpace + 8}px`,
              backgroundSize: `${width}px ${height}px`,
            }"
          >
            <img v-if="blockBackImgBase" :src="`data:image/png;base64,${blockBackImgBase}`" alt="" draggable="false" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import CryptoJS from 'crypto-js';
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';

import { request } from '@/utils/request';

const props = withDefaults(
  defineProps<{
    width?: number;
    height?: number;
    barHeight?: number;
    vSpace?: number;
    explain?: string;
    captchaType?: string;
  }>(),
  {
    width: 310,
    height: 155,
    barHeight: 40,
    vSpace: 5,
    explain: '向右滑动完成验证',
    captchaType: 'blockPuzzle',
  },
);

const emit = defineEmits<{
  (e: 'success', payload: { captchaVerification: string; token: string }): void;
  (e: 'refresh'): void;
}>();

const secretKey = ref('');
const passFlag = ref(false);
const backImgBase = ref('');
const blockBackImgBase = ref('');
const backToken = ref('');
const startMoveTime = ref(0);
const endMoveTime = ref(0);
const tipWords = ref('');
const text = ref(props.explain);
const finishText = ref('');
const moveBlockLeft = ref(0);
const leftBarWidth = ref<number | null>(null);
const moveBlockBackgroundColor = ref('#fff');
const leftBarBorderColor = ref('#ddd');
const iconColor = ref('#000');
const iconClass = ref('icon-right');
const status = ref(false);
const isEnd = ref(false);
const showRefresh = ref(true);
const transitionLeft = ref('');
const transitionWidth = ref('');
const startLeft = ref(0);
const barAreaRef = ref<HTMLDivElement | null>(null);
const backImageWidth = ref(310);
const blockImageWidth = ref(47);

const width = computed(() => props.width);
const height = computed(() => props.height);
const barHeight = computed(() => props.barHeight);
const vSpace = computed(() => props.vSpace);
const blockWidth = computed(() => Math.floor((width.value * 47) / 310));

const aesEncrypt = (word: string, keyWord: string) => {
  const key = CryptoJS.enc.Utf8.parse(keyWord);
  const srcs = CryptoJS.enc.Utf8.parse(word);
  const encrypted = CryptoJS.AES.encrypt(srcs, key, {
    mode: CryptoJS.mode.ECB,
    padding: CryptoJS.pad.Pkcs7,
  });
  return encrypted.toString();
};

const getPointerX = (event: MouseEvent | TouchEvent) => {
  if ('touches' in event && event.touches.length > 0) return event.touches[0].clientX;
  return (event as MouseEvent).clientX;
};

const updateBackImageSize = (base64: string) => {
  if (!base64) return;
  const dataUrl = base64.startsWith('data:') ? base64 : `data:image/png;base64,${base64}`;
  const img = new Image();
  img.onload = () => {
    backImageWidth.value = img.naturalWidth || backImageWidth.value;
  };
  img.src = dataUrl;
};

const updateBlockImageSize = (base64: string) => {
  if (!base64) return;
  const dataUrl = base64.startsWith('data:') ? base64 : `data:image/png;base64,${base64}`;
  const img = new Image();
  img.onload = () => {
    blockImageWidth.value = img.naturalWidth || blockImageWidth.value;
  };
  img.src = dataUrl;
};

const resetState = () => {
  finishText.value = '';
  transitionLeft.value = 'left .3s';
  transitionWidth.value = 'width .3s';
  moveBlockLeft.value = 0;
  leftBarWidth.value = null;
  leftBarBorderColor.value = '#ddd';
  moveBlockBackgroundColor.value = '#fff';
  iconColor.value = '#000';
  iconClass.value = 'icon-right';
  isEnd.value = false;
  passFlag.value = false;

  window.setTimeout(() => {
    transitionWidth.value = '';
    transitionLeft.value = '';
    text.value = props.explain;
  }, 300);
};

const getPicture = async () => {
  try {
    const res = await request.post<{
      repCode: string;
      repMsg?: string;
      repData?: {
        originalImageBase64?: string;
        jigsawImageBase64?: string;
        token?: string;
        secretKey?: string;
      };
    }>(
      { url: '/auth/captcha/get', data: { captchaType: props.captchaType } },
      { isTransformResponse: false, withToken: false },
    );

    if (res.repCode === '0000' && res.repData) {
      backImgBase.value = res.repData.originalImageBase64 || '';
      blockBackImgBase.value = res.repData.jigsawImageBase64 || '';
      backToken.value = res.repData.token || '';
      secretKey.value = res.repData.secretKey || '';
      tipWords.value = '';
      updateBackImageSize(backImgBase.value);
      updateBlockImageSize(blockBackImgBase.value);
      return;
    }

    tipWords.value = res.repMsg || '验证码加载失败';
  } catch {
    tipWords.value = '验证码加载失败';
  }
};

const refresh = () => {
  showRefresh.value = true;
  resetState();
  getPicture();
  emit('refresh');
};

const start = (event: MouseEvent | TouchEvent) => {
  if (isEnd.value || !barAreaRef.value) return;
  const x = getPointerX(event);
  const barRect = barAreaRef.value.getBoundingClientRect();
  startLeft.value = Math.floor(x - barRect.left - moveBlockLeft.value);
  startMoveTime.value = Date.now();
  text.value = '';
  moveBlockBackgroundColor.value = '#337ab7';
  leftBarBorderColor.value = '#337AB7';
  iconColor.value = '#fff';
  status.value = true;
};

const move = (event: MouseEvent | TouchEvent) => {
  if (!status.value || isEnd.value || !barAreaRef.value) return;
  const x = getPointerX(event);
  const barRect = barAreaRef.value.getBoundingClientRect();
  const barWidth = barAreaRef.value.clientWidth || width.value;
  const maxLeft = Math.max(barWidth - barHeight.value, 0);
  let moveLeft = x - barRect.left - startLeft.value;
  if (moveLeft >= maxLeft) {
    moveLeft = maxLeft;
  }
  if (moveLeft <= 0) {
    moveLeft = 0;
  }
  moveBlockLeft.value = moveLeft;
  leftBarWidth.value = moveLeft + Math.floor(barHeight.value / 2);
};

const end = async () => {
  if (!status.value || isEnd.value) return;
  endMoveTime.value = Date.now();
  status.value = false;

  const barWidth = barAreaRef.value?.clientWidth || width.value;
  const baseWidth = backImageWidth.value || 310;
  const baseBlockWidth = blockImageWidth.value || Math.round((blockWidth.value / Math.max(width.value, 1)) * baseWidth);
  const maxCaptchaX = Math.max(baseWidth - baseBlockWidth, 0);
  const mappedX = Math.round((moveBlockLeft.value / Math.max(barWidth, 1)) * baseWidth);
  const moveLeftDistance = Math.min(Math.max(mappedX, 0), maxCaptchaX);
  const point = { x: moveLeftDistance, y: 5.0 };
  const pointJson = secretKey.value ? aesEncrypt(JSON.stringify(point), secretKey.value) : JSON.stringify(point);

  let res: { repCode: string; repMsg?: string };
  try {
    res = await request.post<{
      repCode: string;
      repMsg?: string;
      repData?: { token?: string };
    }>(
      {
        url: '/auth/captcha/check',
        data: {
          captchaType: props.captchaType,
          pointJson,
          token: backToken.value,
        },
      },
      { isTransformResponse: false, withToken: false },
    );
  } catch {
    res = { repCode: '0001', repMsg: '验证失败' };
  }

  if (res.repCode === '0000') {
    moveBlockBackgroundColor.value = '#5cb85c';
    leftBarBorderColor.value = '#5cb85c';
    iconColor.value = '#fff';
    iconClass.value = 'icon-check';
    showRefresh.value = false;
    isEnd.value = true;
    passFlag.value = true;

    const durationSeconds = ((endMoveTime.value - startMoveTime.value) / 1000).toFixed(2);
    tipWords.value = `${durationSeconds}s验证成功`;

    const captchaVerification = secretKey.value
      ? aesEncrypt(`${backToken.value}---${JSON.stringify(point)}`, secretKey.value)
      : `${backToken.value}---${JSON.stringify(point)}`;

    window.setTimeout(() => {
      tipWords.value = '';
      emit('success', { captchaVerification, token: backToken.value });
    }, 1000);
    return;
  }

  moveBlockBackgroundColor.value = '#d9534f';
  leftBarBorderColor.value = '#d9534f';
  iconColor.value = '#fff';
  iconClass.value = 'icon-close';
  passFlag.value = false;
  tipWords.value = res.repMsg || '验证失败';

  window.setTimeout(() => {
    tipWords.value = '';
    refresh();
  }, 1000);
};

const bindEvents = () => {
  window.addEventListener('mousemove', move);
  window.addEventListener('touchmove', move, { passive: false });
  window.addEventListener('mouseup', end);
  window.addEventListener('touchend', end);
};

const unbindEvents = () => {
  window.removeEventListener('mousemove', move);
  window.removeEventListener('touchmove', move);
  window.removeEventListener('mouseup', end);
  window.removeEventListener('touchend', end);
};

onMounted(() => {
  getPicture();
  bindEvents();
});

onBeforeUnmount(() => {
  unbindEvents();
});

watch(
  () => props.captchaType,
  () => {
    refresh();
  },
);
</script>
<style lang="less" scoped>
.drag-captcha {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.verify-img-out {
  position: relative;
}

.verify-img-panel {
  position: relative;
  border-radius: var(--td-radius-default);
  overflow: hidden;
  border: 1px solid var(--td-component-stroke);
  background: var(--td-bg-color-container);

  img {
    width: 100%;
    height: 100%;
    display: block;
    object-fit: fill;
  }
}

.verify-refresh {
  width: 25px;
  height: 25px;
  text-align: center;
  padding: 5px;
  cursor: pointer;
  position: absolute;
  top: 0;
  right: 0;
  z-index: 2;
}

.verify-bar-area {
  position: relative;
  background: #ffffff;
  text-align: center;
  box-sizing: content-box;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.verify-bar-area .verify-move-block {
  position: absolute;
  top: 0;
  left: 0;
  background: #fff;
  cursor: pointer;
  box-sizing: content-box;
  box-shadow: 0 0 2px #888888;
  border-radius: 1px;
}

.verify-bar-area .verify-move-block:hover {
  background-color: #337ab7;
  color: #ffffff;
}

.verify-bar-area .verify-left-bar {
  position: absolute;
  top: -1px;
  left: -1px;
  background: #f0fff0;
  cursor: pointer;
  box-sizing: content-box;
  border: 1px solid #ddd;
}

.verify-bar-area .verify-move-block .verify-sub-block {
  position: absolute;
  text-align: center;
  z-index: 3;
}

.verify-bar-area .verify-move-block .verify-sub-block img {
  width: 100%;
  height: 100%;
  display: block;
}

.verify-bar-area .verify-icon {
  font-size: 18px;
}

.verify-bar-area .verify-msg {
  z-index: 3;
}

.verify-tips {
  position: absolute;
  left: 0;
  bottom: 0;
  width: 100%;
  height: 30px;
  line-height: 30px;
  color: #fff;
}

.suc-bg {
  background-color: rgba(92, 184, 92, 0.5);
}

.err-bg {
  background-color: rgba(217, 83, 79, 0.5);
}

.tips-enter,
.tips-leave-to {
  bottom: -30px;
}

.tips-enter-active,
.tips-leave-active {
  transition: bottom 0.5s;
}

.iconfont {
  font-family: 'iconfont' !important;
  font-size: 16px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.icon-check:before,
.icon-right:before,
.icon-refresh:before {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}

.icon-right:before {
  content: '>';
}

.icon-check:before {
  content: 'OK';
}

.icon-close:before {
  content: 'X';
}

.icon-refresh:before {
  content: 'R';
}
</style>
