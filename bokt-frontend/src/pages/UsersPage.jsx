import { useCallback, useEffect, useState } from 'react';
import {
  Box, Button, Checkbox, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  FormControl, IconButton, InputLabel, ListItemText, MenuItem, OutlinedInput, Paper,
  Select, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TablePagination,
  TableRow, TextField, Tooltip, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import BlockIcon from '@mui/icons-material/Block';
import CheckIcon from '@mui/icons-material/CheckCircle';
import KeyIcon from '@mui/icons-material/VpnKey';
import PageHeader from '../components/PageHeader';
import Mono from '../components/Mono';
import { useFeedback } from '../components/Feedback';
import { GENDERS } from '../constants';
import { usersApi } from '../api/users';
import { rolesApi } from '../api/roles';
import { branchesApi } from '../api/branches';
import { errorMessage } from '../api/client';

export default function UsersPage() {
  const { notify } = useFeedback();
  const [rows, setRows] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [roles, setRoles] = useState([]);
  const [createOpen, setCreateOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await usersApi.list(page, size);
      setRows(data.content); setTotal(data.totalElements);
    } catch (e) { notify(errorMessage(e), 'error'); }
    finally { setLoading(false); }
  }, [page, size, notify]);

  useEffect(() => { load(); }, [load]);
  const [branches, setBranches] = useState([]);
  useEffect(() => { rolesApi.list().then(setRoles).catch(() => {}); }, []);
  useEffect(() => { branchesApi.list().then(setBranches).catch(() => {}); }, []);

  const act = async (fn, msg) => {
    try { await fn(); notify(msg); load(); } catch (e) { notify(errorMessage(e), 'error'); }
  };

  const roleName = (id) => roles.find((r) => r.id === id)?.name || `#${id}`;

  return (
    <Box>
      <PageHeader title="Пользователи" subtitle="Сотрудники организации"
        action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setCreateOpen(true)}>Новый пользователь</Button>} />

      <TableContainer component={Paper} variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell><TableCell>Логин</TableCell><TableCell>ФИО</TableCell>
              <TableCell>Телефон</TableCell><TableCell>Статус</TableCell><TableCell>Роли</TableCell>
              <TableCell align="right">Действия</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((u) => (
              <TableRow key={u.id} hover>
                <TableCell><Mono>{u.id}</Mono></TableCell>
                <TableCell><Mono>{u.username}</Mono>{u.superAdmin && <Chip size="small" label="super" color="secondary" sx={{ ml: 1 }} />}</TableCell>
                <TableCell>{[u.lastName, u.firstName].filter(Boolean).join(' ') || '—'}</TableCell>
                <TableCell><Mono>{u.phone || '—'}</Mono></TableCell>
                <TableCell>
                  <Chip size="small" variant="outlined" color={u.status === 'ACTIVE' ? 'success' : 'default'}
                    label={u.status === 'ACTIVE' ? 'Активен' : 'Отключён'} />
                </TableCell>
                <TableCell>{(u.roleIds || []).map(roleName).join(', ') || '—'}</TableCell>
                <TableCell align="right">
                  {u.status === 'ACTIVE' ? (
                    <Tooltip title="Отключить"><IconButton size="small" color="error"
                      onClick={() => act(() => usersApi.disable(u.id), 'Пользователь отключён')}>
                      <BlockIcon fontSize="small" /></IconButton></Tooltip>
                  ) : (
                    <Tooltip title="Включить"><IconButton size="small" color="success"
                      onClick={() => act(() => usersApi.enable(u.id), 'Пользователь включён')}>
                      <CheckIcon fontSize="small" /></IconButton></Tooltip>
                  )}
                  <Tooltip title="Сбросить пароль (SMS)"><IconButton size="small"
                    onClick={() => act(() => usersApi.resetPassword(u.id), 'Новый пароль отправлен по SMS')}>
                    <KeyIcon fontSize="small" /></IconButton></Tooltip>
                </TableCell>
              </TableRow>
            ))}
            {rows.length === 0 && (
              <TableRow><TableCell colSpan={7}>
                <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                  {loading ? 'Загрузка…' : 'Пользователи не найдены.'}
                </Typography></TableCell></TableRow>
            )}
          </TableBody>
        </Table>
        <TablePagination component="div" count={total} page={page} rowsPerPage={size}
          onPageChange={(_, p) => setPage(p)}
          onRowsPerPageChange={(e) => { setSize(parseInt(e.target.value, 10)); setPage(0); }}
          rowsPerPageOptions={[10, 20, 50]} labelRowsPerPage="Строк:" />
      </TableContainer>

      {createOpen && (
        <CreateUserDialog roles={roles} branches={branches} onClose={() => setCreateOpen(false)}
          onCreated={() => { setCreateOpen(false); load(); notify('Пользователь создан, учётные данные отправлены по SMS'); }} />
      )}
    </Box>
  );
}

function CreateUserDialog({ roles, branches, onClose, onCreated }) {
  const { notify } = useFeedback();
  const [form, setForm] = useState({
    username: '', firstName: '', lastName: '', middleName: '', birthDate: '',
    gender: 'UNKNOWN', phone: '', roleIds: [], branchId: '',
  });
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async () => {
    setBusy(true);
    try {
      await usersApi.create({
        ...form,
        birthDate: form.birthDate || null,
        branchId: form.branchId ? Number(form.branchId) : null,
        roleIds: form.roleIds,
      });
      onCreated();
    } catch (e) { notify(errorMessage(e), 'error'); }
    finally { setBusy(false); }
  };

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Новый пользователь</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Имя пользователя (логин)" value={form.username} onChange={set('username')} />
          <Stack direction="row" spacing={2}>
            <TextField label="Фамилия" value={form.lastName} onChange={set('lastName')} sx={{ flex: 1 }} />
            <TextField label="Имя" value={form.firstName} onChange={set('firstName')} sx={{ flex: 1 }} />
          </Stack>
          <TextField label="Телефон" value={form.phone} onChange={set('phone')} placeholder="+994..." />
          <Stack direction="row" spacing={2}>
            <TextField label="Дата рождения" type="date" value={form.birthDate} onChange={set('birthDate')}
              InputLabelProps={{ shrink: true }} sx={{ flex: 1 }} />
            <TextField select label="Пол" value={form.gender} onChange={set('gender')} sx={{ width: 160 }}>
              {GENDERS.map((g) => <MenuItem key={g.value} value={g.value}>{g.label}</MenuItem>)}
            </TextField>
          </Stack>
          <TextField select label="Филиал" value={form.branchId} onChange={set('branchId')}
            helperText="Директор выбирает филиал модератору. Если создаёт модератор — филиал подставится автоматически.">
            <MenuItem value="">— не задан (вся организация) —</MenuItem>
            {branches.map((b) => <MenuItem key={b.id} value={b.id}>{b.name}</MenuItem>)}
          </TextField>
          <FormControl>
            <InputLabel>Роли</InputLabel>
            <Select multiple value={form.roleIds} input={<OutlinedInput label="Роли" />}
              onChange={(e) => setForm({ ...form, roleIds: e.target.value })}
              renderValue={(sel) => sel.map((id) => roles.find((r) => r.id === id)?.name || id).join(', ')}>
              {roles.map((r) => (
                <MenuItem key={r.id} value={r.id}>
                  <Checkbox checked={form.roleIds.includes(r.id)} />
                  <ListItemText primary={r.name} secondary={r.description} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <Typography variant="caption" color="text.secondary">
            Пароль и OTP сгенерируются автоматически и уйдут пользователю по SMS.
          </Typography>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button variant="contained" onClick={submit} disabled={busy}>Создать</Button>
      </DialogActions>
    </Dialog>
  );
}
