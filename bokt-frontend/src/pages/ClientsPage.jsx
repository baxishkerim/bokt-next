import { useCallback, useEffect, useState } from 'react';
import {
  Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, IconButton,
  MenuItem, Paper, Stack, Table, TableBody, TableCell, TableContainer, TableHead,
  TablePagination, TableRow, TextField, Tooltip, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import PageHeader from '../components/PageHeader.jsx';
import Mono from '../components/Mono.jsx';
import { useAuth } from '../auth/AuthContext.jsx';
import { useFeedback } from '../components/Feedback.jsx';
import { P, GENDERS } from '../constants.js';
import { clientsApi } from '../api/clients.js';
import { errorMessage } from '../api/client.js';

export default function ClientsPage() {
  const { hasAuthority } = useAuth();
  const { notify } = useFeedback();
  const [q, setQ] = useState('');
  const [rows, setRows] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [edit, setEdit] = useState(null); // null | {} (new) | client (edit)
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await clientsApi.search(q, page, size);
      setRows(data.content); setTotal(data.totalElements);
    } catch (e) { notify(errorMessage(e), 'error'); }
    finally { setLoading(false); }
  }, [q, page, size, notify]);

  useEffect(() => { load(); }, [load]);

  return (
    <Box>
      <PageHeader title="Клиенты" subtitle="Заёмщики организации"
        action={hasAuthority(P.CLIENT_MANAGE) && (
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setEdit({})}>Новый клиент</Button>
        )} />

      <TextField label="Поиск (фамилия / телефон / PIN)" value={q} size="small" sx={{ mb: 2, minWidth: 320 }}
        onChange={(e) => { setQ(e.target.value); setPage(0); }} />

      <TableContainer component={Paper} variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell><TableCell>PIN</TableCell><TableCell>ФИО</TableCell>
              <TableCell>Телефон</TableCell><TableCell align="right">Действия</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((c) => (
              <TableRow key={c.id} hover>
                <TableCell><Mono>{c.id}</Mono></TableCell>
                <TableCell><Mono>{c.pin}</Mono></TableCell>
                <TableCell>{[c.lastName, c.firstName, c.middleName].filter(Boolean).join(' ') || '—'}</TableCell>
                <TableCell><Mono>{c.phone || '—'}</Mono></TableCell>
                <TableCell align="right">
                  {hasAuthority(P.CLIENT_MANAGE) && (
                    <Tooltip title="Редактировать"><IconButton size="small" onClick={() => setEdit(c)}>
                      <EditIcon fontSize="small" /></IconButton></Tooltip>
                  )}
                </TableCell>
              </TableRow>
            ))}
            {rows.length === 0 && (
              <TableRow><TableCell colSpan={5}>
                <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                  {loading ? 'Загрузка…' : 'Клиенты не найдены.'}
                </Typography></TableCell></TableRow>
            )}
          </TableBody>
        </Table>
        <TablePagination component="div" count={total} page={page} rowsPerPage={size}
          onPageChange={(_, p) => setPage(p)}
          onRowsPerPageChange={(e) => { setSize(parseInt(e.target.value, 10)); setPage(0); }}
          rowsPerPageOptions={[10, 20, 50]} labelRowsPerPage="Строк:" />
      </TableContainer>

      {edit !== null && (
        <ClientDialog client={edit} onClose={() => setEdit(null)}
          onSaved={() => { setEdit(null); load(); notify('Клиент сохранён'); }} />
      )}
    </Box>
  );
}

function ClientDialog({ client, onClose, onSaved }) {
  const { notify } = useFeedback();
  const isEdit = !!client.id;
  const [form, setForm] = useState({
    pin: client.pin || '', firstName: client.firstName || '', lastName: client.lastName || '',
    middleName: client.middleName || '', birthDate: client.birthDate || '',
    gender: client.gender || 'UNKNOWN', phone: client.phone || '', address: client.address || '',
    secretWord: client.secretWord || '',
  });
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async () => {
    setBusy(true);
    try {
      const body = { ...form, birthDate: form.birthDate || null };
      if (isEdit) await clientsApi.update(client.id, body);
      else await clientsApi.create(body);
      onSaved();
    } catch (e) { notify(errorMessage(e), 'error'); }
    finally { setBusy(false); }
  };

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{isEdit ? `Клиент #${client.id}` : 'Новый клиент'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="PIN (ФИН)" value={form.pin} onChange={set('pin')} disabled={isEdit} />
          <Stack direction="row" spacing={2}>
            <TextField label="Фамилия" value={form.lastName} onChange={set('lastName')} sx={{ flex: 1 }} />
            <TextField label="Имя" value={form.firstName} onChange={set('firstName')} sx={{ flex: 1 }} />
          </Stack>
          <TextField label="Отчество" value={form.middleName} onChange={set('middleName')} />
          <Stack direction="row" spacing={2}>
            <TextField label="Дата рождения" type="date" value={form.birthDate} onChange={set('birthDate')}
              InputLabelProps={{ shrink: true }} sx={{ flex: 1 }} />
            <TextField select label="Пол" value={form.gender} onChange={set('gender')} sx={{ width: 160 }}>
              {GENDERS.map((g) => <MenuItem key={g.value} value={g.value}>{g.label}</MenuItem>)}
            </TextField>
          </Stack>
          <TextField label="Телефон" value={form.phone} onChange={set('phone')} placeholder="+994..." />
          <TextField label="Адрес" value={form.address} onChange={set('address')} />
          <TextField label="Секретное слово" value={form.secretWord} onChange={set('secretWord')} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button variant="contained" onClick={submit} disabled={busy}>Сохранить</Button>
      </DialogActions>
    </Dialog>
  );
}
