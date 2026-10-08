// Хранилище токенов в localStorage + разбор JWT для UI (права/имя/тенант).
// Это обычное веб-приложение (не артефакт), поэтому localStorage допустим.

const ACCESS = 'bokt.accessToken';
const REFRESH = 'bokt.refreshToken';

export const tokenStore = {
  getAccess: () => localStorage.getItem(ACCESS),
  getRefresh: () => localStorage.getItem(REFRESH),
  set: (access, refresh) => {
    if (access) localStorage.setItem(ACCESS, access);
    if (refresh) localStorage.setItem(REFRESH, refresh);
  },
  clear: () => {
    localStorage.removeItem(ACCESS);
    localStorage.removeItem(REFRESH);
  },
};

/** Разбор payload JWT (без верификации — только для отображения в UI). */
export function decodeJwt(token) {
  if (!token) return null;
  try {
    const payload = token.split('.')[1];
    const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(decodeURIComponent(escape(json)));
  } catch {
    return null;
  }
}

/** Пользователь из access-токена: id, username, tenantId, superAdmin, authorities[]. */
export function userFromToken(token) {
  const c = decodeJwt(token);
  if (!c) return null;
  return {
    userId: c.sub ? Number(c.sub) : null,
    username: c.username,
    tenantId: c.tenantId ?? null,
    superAdmin: !!c.superAdmin,
    authorities: c.authorities || [],
    exp: c.exp || 0,
  };
}
