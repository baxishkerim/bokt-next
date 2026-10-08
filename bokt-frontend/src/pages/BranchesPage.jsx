import { useCallback, useEffect, useState } from 'react';
import {
  Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Divider, Paper,
  Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import PageHeader from '../components/PageHeader.jsx';
import { useAuth } from '../auth/AuthContext.jsx';
import { useFeedback } from '../components/Feedback.jsx';
import { P } from '../constants.js';
import { branchesApi } from '../api/branches.js';
import { errorMessage } from '../api/client.js';

const EMPTY = {
  branchName: '', frontId: '',
  moderatorUsername: '', moderatorFirstName: '', moderatorLastName: '', moderatorPhone: '',
};

export default function BranchesPage() {
  const { hasAuthority } = useAuth();
  const { notify } = useFeedback();
  const [rows, setRows] = useState([]);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState(EMPTY);
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try { setRows(await branchesApi.list()); }
    catch (e) { notify(errorMessage(e), 'error'); }
    finally { setLoading(false); }
  }, [notify]);

  useEffect(() => { load(); }, [load]);

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async () => {
    setBusy(true);
    try {
      await branchesApi.add(form);
      setOpen(false); setForm(EMPTY); load();
      notify('Филиал и его модератор созданы, модератору отправлены данные по SMS');
    } catch (e) { notify(errorMessage(e), 'error'); }
    finally { setBusy(false); }
  };

  const ready = form.branchName && form.moderatorUsername && form.moderatorPhone;

  return (
    <Box>
      <PageHeader title="Филиалы" subtitle="Подразделения организации. Филиал создаётся вместе с модератором (начальником)."
        action={hasAuthority(P.USER_MANAGE) && (
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setOpen(true)}>Создать филиал</Button>
        )} />

      <TableContainer component={Paper} variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow><TableCell>ID</TableCell><TableCell>Название</TableCell><TableCell>Front ID</TableCell><TableCell>Статус</TableCell></TableRow>
          </TableHead>
          <TableBody>
            {rows.map((b) => (
              <TableRow key={b.id} hover>
                <TableCell>{b.id}</TableCell>
                <TableCell>{b.name}</TableCell>
                <TableCell>{b.frontId || '—'}</TableCell>
                <TableCell>
                  <Chip size="small" variant="outlined" color={b.status === 'ACTIVE' ? 'success' : 'default'}
                    label={b.status === 'ACTIVE' ? 'Активен' : 'Отключён'} />
                </TableCell>
              </TableRow>
            ))}
            {rows.length === 0 && (
              <TableRow><TableCell colSpan={4}>
                <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                  {loading ? 'Загрузка…' : 'Филиалов нет.'}
                </Typography></TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="sm">
        <DialogTitle>Новый филиал и его модератор</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <Divider textAlign="left"><Typography variant="caption">Филиал</Typography></Divider>
            <TextField label="Название филиала" value={form.branchName} onChange={set('branchName')} />
            <TextField label="Front ID" value={form.frontId} onChange={set('frontId')} />

            <Divider textAlign="left"><Typography variant="caption">Модератор (начальник филиала)</Typography></Divider>
            <TextField label="Логин модератора" value={form.moderatorUsername} onChange={set('moderatorUsername')}
              helperText="Пароль и OTP уйдут ему по SMS" />
            <Stack direction="row" spacing={2}>
              <TextField label="Имя" value={form.moderatorFirstName} onChange={set('moderatorFirstName')} sx={{ flex: 1 }} />
              <TextField label="Фамилия" value={form.moderatorLastName} onChange={set('moderatorLastName')} sx={{ flex: 1 }} />
            </Stack>
            <TextField label="Телефон модератора" value={form.moderatorPhone} onChange={set('moderatorPhone')} placeholder="+994..." />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Отмена</Button>
          <Button variant="contained" onClick={submit} disabled={busy || !ready}>Создать</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
