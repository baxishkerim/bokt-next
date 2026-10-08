// Права (совпадают с enum Permission на бэке)
export const P = {
  CREDIT_ADD: 'CREDIT_ADD',
  CREDIT_APPROVE: 'CREDIT_APPROVE',
  CREDIT_CANCEL: 'CREDIT_CANCEL',
  CREDIT_VIEW: 'CREDIT_VIEW',
  CREDIT_ARCHIVE_VIEW: 'CREDIT_ARCHIVE_VIEW',
  CREDIT_BY_PAN: 'CREDIT_BY_PAN',
  CARD_LIST: 'CARD_LIST',
  CARD_TRANSACTIONS: 'CARD_TRANSACTIONS',
  CLIENT_MANAGE: 'CLIENT_MANAGE',
  FILE_IMPORT: 'FILE_IMPORT',
  REVERSAL_EXECUTE: 'REVERSAL_EXECUTE',
  CARD_MANAGE: 'CARD_MANAGE',
  REPORT_VIEW: 'REPORT_VIEW',
  USER_MANAGE: 'USER_MANAGE',
  ROLE_MANAGE: 'ROLE_MANAGE',
  TENANT_MANAGE: 'TENANT_MANAGE',
};

export const ALL_PERMISSIONS = Object.values(P);

// Статусы кредита + цвета чипов (MUI color)
export const CREDIT_STATUS = {
  NEW: { label: 'Новый', color: 'info' },
  APPROVED: { label: 'Подтверждён', color: 'success' },
  CANCELLED: { label: 'Отменён', color: 'error' },
  POSTPONED: { label: 'Отложен', color: 'warning' },
};

export const GENDERS = [
  { value: 'MALE', label: 'Мужской' },
  { value: 'FEMALE', label: 'Женский' },
  { value: 'UNKNOWN', label: 'Не указан' },
];

// Валюты из seed-миграции (id → код). currencyId=1 обычно AZN.
export const CURRENCIES = [
  { id: 1, code: '944', label: 'AZN' },
  { id: 2, code: '840', label: 'USD' },
  { id: 3, code: '978', label: 'EUR' },
];
