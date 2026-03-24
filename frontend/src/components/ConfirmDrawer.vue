<template>
  <t-drawer
    v-bind="attrs"
    v-model:visible="proxyVisible"
    :close-on-overlay-click="false"
    @overlay-click="handleOverlayClick"
  >
    <template v-for="(_, name) in $slots" #[name]="slotProps">
      <slot :name="name" v-bind="slotProps" />
    </template>
  </t-drawer>
</template>
<script setup lang="ts">
import { DialogPlugin } from 'tdesign-vue-next';
import { computed, useAttrs } from 'vue';

interface Props {
  visible?: boolean;
  confirmOnOverlayClick?: boolean;
  overlayConfirmTitle?: string;
  overlayConfirmBody?: string;
  overlayConfirmBtn?: string;
  overlayCancelBtn?: string;
}

interface Emits {
  (e: 'update:visible', v: boolean): void;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  confirmOnOverlayClick: true,
  overlayConfirmTitle: '提示',
  overlayConfirmBody: '关闭抽屉将丢失未保存的内容，是否取消编辑？',
  overlayConfirmBtn: '取消编辑',
  overlayCancelBtn: '继续编辑',
});

const emit = defineEmits<Emits>();
const attrs = useAttrs();

const proxyVisible = computed({
  get: () => props.visible,
  set: (v) => emit('update:visible', v),
});

const invokeHandler = (handler: unknown, ...args: any[]) => {
  if (typeof handler === 'function') handler(...args);
  else if (Array.isArray(handler)) handler.forEach((fn) => typeof fn === 'function' && fn(...args));
};

const handleOverlayClick = (ctx: any) => {
  const onOverlayClick = (attrs as any).onOverlayClick;
  invokeHandler(onOverlayClick, ctx);

  if (!props.confirmOnOverlayClick) return;

  const dialog = DialogPlugin.confirm({
    header: props.overlayConfirmTitle,
    body: props.overlayConfirmBody,
    confirmBtn: props.overlayConfirmBtn,
    cancelBtn: props.overlayCancelBtn,
    closeOnOverlayClick: true,
    onConfirm: () => {
      dialog.hide();
      proxyVisible.value = false;
      const onClose = (attrs as any).onClose;
      invokeHandler(onClose, { trigger: 'overlay', e: ctx?.e ?? ctx });
    },
    onClose: () => dialog.hide(),
  });
};
</script>
