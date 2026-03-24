import { MessagePlugin } from 'tdesign-vue-next';

import { deleteAnnouncement, publishAnnouncement } from '@/api/announcement';

export const useAnnouncementDialog = (reload: () => Promise<void>) => {
  const togglePublish = async (row: any) => {
    const publish = row.status !== 'published';
    await publishAnnouncement(row.id, publish);
    MessagePlugin.success(publish ? '已发布' : '已撤回');
    await reload();
  };

  const handleDelete = async (id: number) => {
    await deleteAnnouncement(id);
    MessagePlugin.success('已删除');
    await reload();
  };

  return { togglePublish, handleDelete };
};
