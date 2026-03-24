import { MessagePlugin } from 'tdesign-vue-next';
import { computed, type Ref } from 'vue';

import { updateMyProfile } from '@/api/user';
import { useUserStore } from '@/store';

import type { UserProfile } from '@/api/user';

export const useAvatarUpload = (profile: Ref<UserProfile>) => {
  const userStore = useUserStore();
  const uploadHeaders = computed(() => ({ Authorization: userStore.token }));

  const handleAvatarSuccess = async (context: { response?: { data?: { url?: string } } }) => {
    const url = context.response?.data?.url;
    if (!url) return;
    try {
      await updateMyProfile({ avatar: url });
      profile.value.avatar = url;
      userStore.userInfo.avatar = url;
      MessagePlugin.success('头像更新成功');
    } catch {
      MessagePlugin.error('头像更新失败');
    }
  };

  const handleAvatarFail = (context: { response?: { data?: { message?: string }; statusText?: string } }) => {
    const msg = context.response?.data?.message || context.response?.statusText || '上传失败';
    MessagePlugin.error(`头像上传失败: ${msg}`);
  };

  return { uploadHeaders, handleAvatarSuccess, handleAvatarFail };
};
