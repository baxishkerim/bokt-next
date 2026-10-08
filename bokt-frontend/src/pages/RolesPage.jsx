import { useCallback, useEffect, useState } from 'react';
import {
  Box, Button, Checkbox, Chip, Dialog, DialogActions, DialogContent, DialogTitle,
  FormControl, IconButton, InputLabel, ListItemText, MenuItem, OutlinedInput, Paper,
  Select, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  TextField, Tooltip, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import PageHeader from '../components/PageHeader.jsx';
import { useFeedback } from '../components/Feedback.jsx';
import { ALL_PERMISSIONS } from '../constants.js';
import { rolesApi } from '../api/roles.js';
import { errorMessage } from '../api/client.js';

export default function RolesPage() {
  const { notify } = useFeedback();
  const [rows, setRows] = useState([]);
  const [createOpen, setCreateOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try { setRows(await rolesApi.list()); }
    catch (e) { notify(errorMessage(e), 'error'); }
    finally { setLoading(false); }
  }, [notify]);

  useEffect(() => { load(); }, [load]);

  const remove = async (id) => {
    try { await rolesApi.remove(id); notify('Роль удалена'); load(); }
    catch (e) { notify(errorMessage(e), 'error'); }
  };

  return (
    <Box>
      <PageHeader title="Роли" subtitle="Наборы прав внутри организации"
        action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setCreateOpen(true)}>Новая роль</Button>} />

      <TableContainer component={Paper} variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell><TableCell>Название</TableCell><TableCell>Описание</TableCell>
              <TableCell>Права</TableCell><TableCell align="right">Действия</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((r) => (
              <TableRow key={r.id} hover>
                <TableCell>{r.id}</TableCell>
                <TableCell>{r.name}</TableCell>
                <TableCell>{r.description || '—'}</TableCell>
                <TableCell sx={{ maxWidth: 420 }}>
                  <Stack direction="row" spacing={0.5} flexWrap="wrap" useFlexGap>
                    {(r.permissions || []).map((p) => <Chip key={p} label={p} size="small" variant="outlined" />)}
                  </Stack>
                </TableCell>
                <TableCell align="right">
                  <Tooltip title="Удалить"><IconButton size="small" color="error" onClick={() => remove(r.id)}>
                    <DeleteIcon fontSize="small" /></IconButton></Tooltip>
                </TableCell>
              </TableRow>
            ))}
            {rows.length === 0 && (
              <TableRow><TableCell colSpan={5}>
                <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                  {loading ? 'Загрузка…' : 'Ролей нет.'}
                </Typography></TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {createOpen && (
        <CreateRoleDialog onClose={() => setCreateOpen(false)}
          onCreated={() => { setCreateOpen(false); load(); notify('Роль создана'); }} />
      )}
    </Box>
  );
}

function CreateRoleDialog({ onClose, onCreated }) {
  const { notify } = useFeedback();
  const [form, setForm] = useState({ name: '', description: '', permissions: [] });
  const [busy, setBusy] = useState(false);

  const submit = async () => {
    setBusy(true);
    try { await rolesApi.create(form); onCreated(); }
    catch (e) { notify(errorMessage(e), 'error'); }
    finally { setBusy(false); }
  };

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Новая роль</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Название" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <TextField label="Описание" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          <FormControl>
            <InputLabel>Права</InputLabel>
            <Select multiple value={form.permissions} input={<OutlinedInput label="Права" />}
              onChange={(e) => setForm({ ...form, permissions: e.target.value })}
              renderValue={(sel) => `${sel.length} выбрано`}>
              {ALL_PERMISSIONS.map((p) => (
                <MenuItem key={p} value={p}>
                  <Checkbox checked={form.permissions.includes(p)} />
                  <ListItemText primary={p} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button variant="contained" onClick={submit} disabled={busy || !form.name || form.permissions.length === 0}>
          Создать
        </Button>
      </DialogActions>
    </Dialog>
  );
}
