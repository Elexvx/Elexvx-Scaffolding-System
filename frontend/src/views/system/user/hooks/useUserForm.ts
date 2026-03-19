import { reactive } from 'vue';

export const createUserForm = () =>
  reactive({
    account: '',
    name: '',
    nickname: '',
    gender: '',
    password: '',
    roles: [] as string[],
    mobile: '',
    email: '',
    idType: '',
    idCard: '',
    idValidFrom: '' as string | '',
    idValidTo: '' as string | '',
    joinDay: '' as string | '',
    team: '',
    provinceId: null as number | null,
    province: '',
    cityId: null as number | null,
    city: '',
    districtId: null as number | null,
    district: '',
    zipCode: '',
    address: '',
    orgUnitIds: [] as number[],
    departmentIds: [] as number[],
    status: 1,
  });

export const createResetPasswordForm = () =>
  reactive({
    password: '',
  });
