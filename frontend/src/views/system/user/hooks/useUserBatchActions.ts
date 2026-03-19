import { request } from '@/utils/request';

export const useUserBatchActions = () => {
  const removeUsers = async (userIds: number[]) => {
    await Promise.all(userIds.map((id) => request.delete({ url: `/system/user/${id}` })));
  };

  const updateUsersStatus = async (userIds: number[], status: number) => {
    await Promise.all(
      userIds.map((id) =>
        request.put({
          url: `/system/user/${id}`,
          data: { status },
        }),
      ),
    );
  };

  return {
    removeUsers,
    updateUsersStatus,
  };
};

