import { MessagePlugin } from 'tdesign-vue-next';

import { deleteNotification, publishNotification } from '@/api/notification';

export const useNotificationDialog = (reload: () => Promise<void>) => {
  const togglePublish = async (row: any) => {
    const publish = row.status !== 'published';
    await publishNotification(row.id, publish);
    MessagePlugin.success(publish ? '已发布' : '已撤回');
    await reload();
  };

  const handleDelete = async (id: number) => {
    await deleteNotification(id);
    MessagePlugin.success('已删除');
    await reload();
  };

  return { togglePublish, handleDelete };
};
