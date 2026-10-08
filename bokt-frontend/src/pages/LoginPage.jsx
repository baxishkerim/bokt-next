import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import {
  Alert, Box, Button, Card, CardContent, Link, Stack, TextField, Typography,
} from '@mui/material';
import { useAuth } from '../auth/AuthContext.jsx';
import { errorMessage } from '../api/client.js';

export default function LoginPage() {
  const { login, verifyOtp, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/';

  const [step, setStep] = useState('credentials'); // 'credentials' | 'otp'
  const [orgLogin, setOrgLogin] = useState('platform');
  const [username, setUsername] = useState('superadmin');
  const [password, setPassword] = useState('');
  const [reference, setReference] = useState('');
  const [ttl, setTtl] = useState(0);
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  if (isAuthenticated) {
    navigate(from, { replace: true });
  }

  const submitCredentials = async (e) => {
    e.preventDefault();
    setError(''); setBusy(true);
    try {
      const res = await login(orgLogin.trim(), username.trim(), password);
      setReference(res.reference);
      setTtl(res.otpTtlSeconds);
      setStep('otp');
    } catch (err) {
      setError(errorMessage(err, 'Не удалось войти'));
    } finally {
      setBusy(false);
    }
  };

  const submitOtp = async (e) => {
    e.preventDefault();
    setError(''); setBusy(true);
    try {
      await verifyOtp(reference, code.trim());
      navigate(from, { replace: true });
    } catch (err) {
      setError(errorMessage(err, 'Неверный код'));
    } finally {
      setBusy(false);
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', display: 'grid', placeItems: 'center', bgcolor: 'background.default', p: 2 }}>
      <Card sx={{ width: 420, maxWidth: '100%', border: '1px solid', borderColor: 'divider' }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h5" sx={{ color: 'primary.main' }}>
            BOKT<span style={{ color: '#0E8A6B' }}>·</span>next
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            {step === 'credentials' ? 'Вход в консоль операций' : 'Подтвердите вход кодом из SMS'}
          </Typography>

          {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

          {step === 'credentials' ? (
            <form onSubmit={submitCredentials}>
              <Stack spacing={2}>
                <TextField label="Логин организации" value={orgLogin}
                  onChange={(e) => setOrgLogin(e.target.value)} fullWidth autoFocus />
                <TextField label="Имя пользователя" value={username}
                  onChange={(e) => setUsername(e.target.value)} fullWidth />
                <TextField label="Пароль" type="password" value={password}
                  onChange={(e) => setPassword(e.target.value)} fullWidth />
                <Button type="submit" variant="contained" size="large" disabled={busy}>
                  {busy ? 'Проверка…' : 'Продолжить'}
                </Button>
              </Stack>
            </form>
          ) : (
            <form onSubmit={submitOtp}>
              <Stack spacing={2}>
                <Alert severity="info">
                  Код отправлен по SMS. В dev-режиме он печатается в лог приложения
                  (заглушка SMS). Срок действия — {Math.round(ttl / 60)} мин.
                </Alert>
                <TextField label="Код из SMS" value={code} autoFocus
                  onChange={(e) => setCode(e.target.value)} fullWidth
                  inputProps={{ inputMode: 'numeric', maxLength: 6 }} />
                <Button type="submit" variant="contained" size="large" disabled={busy}>
                  {busy ? 'Вход…' : 'Войти'}
                </Button>
                <Link component="button" type="button" onClick={() => { setStep('credentials'); setCode(''); setError(''); }}>
                  Назад
                </Link>
              </Stack>
            </form>
          )}
        </CardContent>
      </Card>
    </Box>
  );
}
