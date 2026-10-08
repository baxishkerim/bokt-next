import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { authApi } from '../api/auth.js';
import { tokenStore, userFromToken } from '../api/tokenStore.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => userFromToken(tokenStore.getAccess()));

  // синхронизация между вкладками
  useEffect(() => {
    const onStorage = () => setUser(userFromToken(tokenStore.getAccess()));
    window.addEventListener('storage', onStorage);
    return () => window.removeEventListener('storage', onStorage);
  }, []);

  const value = useMemo(() => {
    const authorities = user?.authorities || [];
    const hasAuthority = (a) => user?.superAdmin || authorities.includes(a);
    const hasAny = (list) => user?.superAdmin || list.some((a) => authorities.includes(a));

    return {
      user,
      isAuthenticated: !!user,
      hasAuthority,
      hasAny,

      /** Шаг 1 входа. Возвращает { reference, otpTtlSeconds }. */
      login: (orgLogin, username, password) => authApi.login(orgLogin, username, password),

      /** Шаг 2 входа: подтверждение OTP → сохраняем токены и пользователя. */
      verifyOtp: async (reference, code) => {
        const tokens = await authApi.verifyOtp(reference, code);
        tokenStore.set(tokens.accessToken, tokens.refreshToken);
        setUser(userFromToken(tokens.accessToken));
        return tokens;
      },

      logout: async () => {
        try { await authApi.logout(); } catch { /* игнорируем сетевые ошибки при выходе */ }
        tokenStore.clear();
        setUser(null);
      },
    };
  }, [user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
