import { useCallback, useEffect, useState } from 'react';
import {
  Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Divider, IconButton,
  Paper, Stack, Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, TextField, Tooltip, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import ToggleOnIcon from '@mui/icons-material/ToggleOn';
import ToggleOffIcon from '@mui/icons-material/ToggleOff';
import PageHeader from '../components/PageHeader.jsx';
import Mono from '../components/Mono.jsx';
import { useFeedback } from '../components/Feedback.jsx';
import { organisationsApi } from '../api/organisations.js';
import { errorMessage } from '../api/client.js';

export default function OrganisationsPage() {
  const { notify } = useFeedback();
  const [rows, setRows] = useState([]);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try { setRows(await organisationsApi.list()); }
    catch (e) { notify(errorMessage(e), 'error'); }
    finally { setLoading(false); }
  }, [notify]);

  useEffect(() => { load(); }, [load]);

  const toggle = async (o) => {
    try {
      if (o.active) await organisationsApi.deactivate(o.id);
      else await organisationsApi.activate(o.id);
      notify(o.active ? 'Организация отключена' : 'Организация активирована'); load();
    } catch (e) { notify(errorMessage(e), 'error'); }
  };

  return (
    <Box>
      <PageHeader title="Организации (NBCO)" subtitle="Тенанты платформы — только супер-админ"
        action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setOpen(true)}>Зарегистрировать NBCO</Button>} />

      <TableContainer component={Paper} variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell><TableCell>Название</TableCell><TableCell>Логин</TableCell>
              <TableCell>Front ID</TableCell><TableCell>Статус</TableCell><TableCell align="right">Действия</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((o) => (
              <TableRow key={o.id} hover>
                <TableCell>{o.id}</TableCell>
                <TableCell>{o.name}{o.platform && <Chip size="small" label="платформа" sx={{ ml: 1 }} />}</TableCell>
                <TableCell><Mono>{o.login}</Mono></TableCell>
                <TableCell><Mono>{o.frontId || '—'}</Mono></TableCell>
                <TableCell>
                  <Chip size="small" variant="outlined" color={o.active ? 'success' : 'default'}
                    label={o.active ? 'Активна' : 'Отключена'} />
                </TableCell>
                <TableCell align="right">
                  {!o.platform && (
                    <Tooltip title={o.active ? 'Отключить' : 'Активировать'}>
                      <IconButton size="small" color={o.active ? 'error' : 'success'} onClick={() => toggle(o)}>
                        {o.active ? <ToggleOffIcon /> : <ToggleOnIcon />}
                      </IconButton>
                    </Tooltip>
                  )}
                </TableCell>
              </TableRow>
            ))}
            {rows.length === 0 && (
              <TableRow><TableCell colSpan={6}>
                <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                  {loading ? 'Загрузка…' : 'Организаций нет.'}
                </Typography></TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {open && (
        <RegisterDialog onClose={() => setOpen(false)}
          onCreated={() => { setOpen(false); load(); notify('NBCO зарегистрирована, директору отправлены данные по SMS'); }} />
      )}
    </Box>
  );
}

function RegisterDialog({ onClose, onCreated }) {
  const { notify } = useFeedback();
  const [form, setForm] = useState({
    name: '', login: '', frontId: '',
    motherCardPan: '', motherCardExpiry: '', merchant: '',
    payoutCardPan: '', payoutCardExpiry: '', payoutInitialBalance: '',
    bins: '', headBranchName: 'Head Office',
    directorUsername: '', directorFirstName: '', directorLastName: '', directorPhone: '',
  });
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async () => {
    setBusy(true);
    try {
      await organisationsApi.register({
        name: form.name, login: form.login, frontId: form.frontId || null,
        motherCardPan: form.motherCardPan, motherCardExpiry: form.motherCardExpiry || null,
        merchant: form.merchant || null,
        payoutCardPan: form.payoutCardPan, payoutCardExpiry: form.payoutCardExpiry || null,
        payoutInitialBalance: form.payoutInitialBalance ? Number(form.payoutInitialBalance) : 0,
        bins: form.bins ? form.bins.split(',').map((s) => s.trim()).filter(Boolean) : [],
        headBranchName: form.headBranchName || null,
        directorUsername: form.directorUsername,
        directorFirstName: form.directorFirstName || null,
        directorLastName: form.directorLastName || null,
        directorPhone: form.directorPhone,
      });
      onCreated();
    } catch (e) { notify(errorMessage(e), 'error'); }
    finally { setBusy(false); }
  };

  const ready = form.name && form.login && form.motherCardPan && form.payoutCardPan
    && form.directorUsername && form.directorPhone;

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Регистрация NBCO</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <Typography variant="subtitle2" color="text.secondary">Организация</Typography>
          <TextField label="Название" value={form.name} onChange={set('name')} />
          <Stack direction="row" spacing={2}>
            <TextField label="Логин организации" value={form.login} onChange={set('login')} sx={{ flex: 1 }}
              helperText="Используется при входе" />
            <TextField label="Front ID" value={form.frontId} onChange={set('frontId')} sx={{ width: 140 }} />
          </Stack>

          <Divider textAlign="left"><Typography variant="caption">Материнская карта (списание)</Typography></Divider>
          <Stack direction="row" spacing={2}>
            <TextField label="PAN" value={form.motherCardPan} onChange={set('motherCardPan')} sx={{ flex: 1 }} />
            <TextField label="Срок (MMYY)" value={form.motherCardExpiry} onChange={set('motherCardExpiry')} sx={{ width: 120 }} />
          </Stack>
          <TextField label="Мерчант / терминал" value={form.merchant} onChange={set('merchant')} />

          <Divider textAlign="left"><Typography variant="caption">Карта выдачи (клиент снимает)</Typography></Divider>
          <Stack direction="row" spacing={2}>
            <TextField label="PAN" value={form.payoutCardPan} onChange={set('payoutCardPan')} sx={{ flex: 1 }} />
            <TextField label="Срок (MMYY)" value={form.payoutCardExpiry} onChange={set('payoutCardExpiry')} sx={{ width: 120 }} />
          </Stack>
          <TextField label="Начальный лимит выдачи (AZN)" type="number" value={form.payoutInitialBalance}
            onChange={set('payoutInitialBalance')} inputProps={{ step: '0.01', min: '0' }} />

          <Divider textAlign="left"><Typography variant="caption">Директор (первый пользователь)</Typography></Divider>
          <TextField label="Логин директора" value={form.directorUsername} onChange={set('directorUsername')}
            helperText="Пароль и OTP уйдут ему по SMS" />
          <Stack direction="row" spacing={2}>
            <TextField label="Имя" value={form.directorFirstName} onChange={set('directorFirstName')} sx={{ flex: 1 }} />
            <TextField label="Фамилия" value={form.directorLastName} onChange={set('directorLastName')} sx={{ flex: 1 }} />
          </Stack>
          <TextField label="Телефон директора" value={form.directorPhone} onChange={set('directorPhone')} placeholder="+994..." />

          <Divider textAlign="left"><Typography variant="caption">Прочее</Typography></Divider>
          <TextField label="BIN-ы клиентских карт (через запятую)" value={form.bins} onChange={set('bins')} />
          <TextField label="Головной филиал" value={form.headBranchName} onChange={set('headBranchName')} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button variant="contained" onClick={submit} disabled={busy || !ready}>Зарегистрировать</Button>
      </DialogActions>
    </Dialog>
  );
}
