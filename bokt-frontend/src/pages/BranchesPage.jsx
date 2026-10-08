import { useCallback, useEffect, useState } from 'react';
import {
  Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Paper,
  Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import PageHeader from '../components/PageHeader.jsx';
import { useAuth } from '../auth/AuthContext.jsx';
import { useFeedback } from '../components/Feedback.jsx';
import { P } from '../constants.js';
import { branchesApi } from '../api/branches.js';
import { errorMessage } from '../api/client.js';

export default function BranchesPage() {
  const { hasAuthority } = useAuth();
  const { notify } = useFeedback();
  const [rows, setRows] = useState([]);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ name: '', frontId: '' });
  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try { setRows(await branchesApi.list()); }
    catch (e) { notify(errorMessage(e), 'error'); }
    finally { setLoading(false); }
  }, [notify]);

  useEffect(() => { load(); }, [load]);

  const submit = async () => {
    setBusy(true);
    try { await branchesApi.add(form); setOpen(false); setForm({ name: '', frontId: '' }); load(); notify('Филиал добавлен'); }
    catch (e) { notify(errorMessage(e), 'error'); }
    finally { setBusy(false); }
  };

  return (
    <Box>
      <PageHeader title="Филиалы" subtitle="Подразделения организации"
        action={hasAuthority(P.USER_MANAGE) && (
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setOpen(true)}>Добавить филиал</Button>
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

      <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="xs">
        <DialogTitle>Новый филиал</DialogTitle>
        <DialogContent>
          <Stack spacing={2} sx={{ mt: 1 }}>
            <TextField label="Название" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            <TextField label="Front ID" value={form.frontId} onChange={(e) => setForm({ ...form, frontId: e.target.value })} />
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)}>Отмена</Button>
          <Button variant="contained" onClick={submit} disabled={busy || !form.name}>Добавить</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
