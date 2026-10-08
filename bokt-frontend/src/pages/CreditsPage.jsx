import { useCallback, useEffect, useState } from 'react';
import {
  Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, IconButton,
  MenuItem, Paper, Stack, Table, TableBody, TableCell, TableContainer, TableHead,
  TablePagination, TableRow, TextField, Tooltip, Typography,
} from '@mui/material';
import CheckIcon from '@mui/icons-material/CheckCircle';
import CancelIcon from '@mui/icons-material/Cancel';
import ScheduleIcon from '@mui/icons-material/Schedule';
import UndoIcon from '@mui/icons-material/Undo';
import AddIcon from '@mui/icons-material/Add';
import PageHeader from '../components/PageHeader.jsx';
import StatusChip from '../components/StatusChip.jsx';
import Mono from '../components/Mono.jsx';
import { useAuth } from '../auth/AuthContext.jsx';
import { useFeedback } from '../components/Feedback.jsx';
import { P, CREDIT_STATUS, CURRENCIES } from '../constants.js';
import { creditsApi } from '../api/credits.js';
import { errorMessage } from '../api/client.js';

const STATUS_OPTIONS = [{ value: '', label: 'Все статусы' },
  ...Object.entries(CREDIT_STATUS).map(([value, s]) => ({ value, label: s.label }))];

export default function CreditsPage() {
  const { hasAuthority } = useAuth();
  const { notify } = useFeedback();

  const [status, setStatus] = useState('');
  const [pan, setPan] = useState('');
  const [rows, setRows] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [loading, setLoading] = useState(false);

  const [createOpen, setCreateOpen] = useState(false);
  const [reversal, setReversal] = useState(null); // { credit }

  const load = useCallback(async () => {
    setLoading(true);
    try {
      if (pan.trim()) {
        const list = await creditsApi.byPan(pan.trim());
        setRows(list); setTotal(list.length);
      } else {
        const data = await creditsApi.list(status, page, size);
        setRows(data.content); setTotal(data.totalElements);
      }
    } catch (e) {
      notify(errorMessage(e), 'error');
    } finally {
      setLoading(false);
    }
  }, [status, pan, page, size, notify]);

  useEffect(() => { load(); }, [load]);

  const act = async (fn, okMsg) => {
    try { await fn(); notify(okMsg); load(); }
    catch (e) { notify(errorMessage(e), 'error'); }
  };

  return (
    <Box>
      <PageHeader
        title="Кредиты"
        subtitle="Заявки на мгновенный кредит: создание, подтверждение, отмена, возврат"
        action={hasAuthority(P.CREDIT_ADD) && (
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setCreateOpen(true)}>
            Новая заявка
          </Button>
        )}
      />

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }}>
        <TextField select label="Статус" value={status} size="small" sx={{ minWidth: 200 }}
          onChange={(e) => { setStatus(e.target.value); setPage(0); }} disabled={!!pan.trim()}>
          {STATUS_OPTIONS.map((o) => <MenuItem key={o.value} value={o.value}>{o.label}</MenuItem>)}
        </TextField>
        <TextField label="Поиск по карте (PAN)" value={pan} size="small" sx={{ minWidth: 260 }}
          onChange={(e) => setPan(e.target.value)}
          helperText="Нужно право CREDIT_BY_PAN" />
      </Stack>

      <TableContainer component={Paper} variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell>
              <TableCell>Клиент</TableCell>
              <TableCell>Карта выдачи</TableCell>
              <TableCell align="right">Сумма</TableCell>
              <TableCell>Статус</TableCell>
              <TableCell>RRN (topup)</TableCell>
              <TableCell align="right">Действия</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((c) => (
              <TableRow key={c.id} hover>
                <TableCell><Mono>{c.id}</Mono></TableCell>
                <TableCell><Mono>{c.clientId}</Mono></TableCell>
                <TableCell><Mono>{c.maskedClientCardPan}</Mono></TableCell>
                <TableCell align="right"><Mono sx={{ fontWeight: 600 }}>{Number(c.amount).toFixed(2)}</Mono></TableCell>
                <TableCell><StatusChip status={c.status} /></TableCell>
                <TableCell><Mono sx={{ color: 'text.secondary' }}>{c.topupRrn || '—'}</Mono></TableCell>
                <TableCell align="right">
                  {c.status === 'NEW' && hasAuthority(P.CREDIT_APPROVE) && (
                    <Tooltip title="Подтвердить (провести платёж)"><IconButton size="small" color="success"
                      onClick={() => act(() => creditsApi.approve(c.id), `Кредит #${c.id} подтверждён`)}>
                      <CheckIcon fontSize="small" /></IconButton></Tooltip>
                  )}
                  {c.status === 'NEW' && hasAuthority(P.CREDIT_ADD) && (
                    <Tooltip title="Отложить"><IconButton size="small" color="warning"
                      onClick={() => act(() => creditsApi.postpone(c.id), `Кредит #${c.id} отложен`)}>
                      <ScheduleIcon fontSize="small" /></IconButton></Tooltip>
                  )}
                  {(c.status === 'NEW' || c.status === 'POSTPONED') && hasAuthority(P.CREDIT_CANCEL) && (
                    <Tooltip title="Отменить"><IconButton size="small" color="error"
                      onClick={() => act(() => creditsApi.cancel(c.id), `Кредит #${c.id} отменён`)}>
                      <CancelIcon fontSize="small" /></IconButton></Tooltip>
                  )}
                  {c.status === 'APPROVED' && hasAuthority(P.REVERSAL_EXECUTE) && (
                    <Tooltip title="Возврат"><IconButton size="small"
                      onClick={() => setReversal({ credit: c })}>
                      <UndoIcon fontSize="small" /></IconButton></Tooltip>
                  )}
                </TableCell>
              </TableRow>
            ))}
            {rows.length === 0 && (
              <TableRow><TableCell colSpan={7}>
                <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                  {loading ? 'Загрузка…' : 'Кредитов не найдено. Создайте заявку или измените фильтр.'}
                </Typography>
              </TableCell></TableRow>
            )}
          </TableBody>
        </Table>
        {!pan.trim() && (
          <TablePagination component="div" count={total} page={page} rowsPerPage={size}
            onPageChange={(_, p) => setPage(p)}
            onRowsPerPageChange={(e) => { setSize(parseInt(e.target.value, 10)); setPage(0); }}
            rowsPerPageOptions={[10, 20, 50]} labelRowsPerPage="Строк:" />
        )}
      </TableContainer>

      <CreateCreditDialog open={createOpen} onClose={() => setCreateOpen(false)}
        onCreated={() => { setCreateOpen(false); load(); notify('Заявка создана'); }} />
      <ReversalDialog data={reversal} onClose={() => setReversal(null)}
        onDone={() => { setReversal(null); load(); notify('Возврат выполнен'); }} />
    </Box>
  );
}

function CreateCreditDialog({ open, onClose, onCreated }) {
  const { notify } = useFeedback();
  const empty = { clientId: '', amount: '', currencyId: 1, secretWord: '', description: '' };
  const [form, setForm] = useState(empty);
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async () => {
    setBusy(true);
    try {
      await creditsApi.add({
        clientId: Number(form.clientId),
        amount: Number(form.amount),
        currencyId: Number(form.currencyId),
        secretWord: form.secretWord || null,
        description: form.description || null,
      });
      setForm(empty); onCreated();
    } catch (e) { notify(errorMessage(e), 'error'); }
    finally { setBusy(false); }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Новая заявка на кредит</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="ID клиента" value={form.clientId} onChange={set('clientId')}
            helperText="Найдите клиента в разделе «Клиенты»" />
          <Stack direction="row" spacing={2}>
            <TextField label="Сумма" type="number" value={form.amount} onChange={set('amount')}
              inputProps={{ step: '0.01', min: '0' }} sx={{ flex: 1 }} />
            <TextField select label="Валюта" value={form.currencyId} onChange={set('currencyId')} sx={{ width: 140 }}>
              {CURRENCIES.map((c) => <MenuItem key={c.id} value={c.id}>{c.label}</MenuItem>)}
            </TextField>
          </Stack>
          <TextField label="Секретное слово" value={form.secretWord} onChange={set('secretWord')} />
          <TextField label="Описание" value={form.description} onChange={set('description')} multiline rows={2} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button variant="contained" onClick={submit} disabled={busy}>Создать</Button>
      </DialogActions>
    </Dialog>
  );
}

function ReversalDialog({ data, onClose, onDone }) {
  const { notify } = useFeedback();
  const [amount, setAmount] = useState('');
  const [reason, setReason] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => { setAmount(''); setReason(''); }, [data]);
  if (!data) return null;

  const submit = async () => {
    setBusy(true);
    try {
      await creditsApi.reversal(data.credit.id, {
        amount: amount ? Number(amount) : null,
        reason: reason || null,
      });
      onDone();
    } catch (e) { notify(errorMessage(e), 'error'); }
    finally { setBusy(false); }
  };

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>Возврат по кредиту #{data.credit.id}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <Typography variant="body2" color="text.secondary">
            Оставьте сумму пустой для полного возврата ({Number(data.credit.amount).toFixed(2)}),
            либо укажите сумму для частичного.
          </Typography>
          <TextField label="Сумма возврата" type="number" value={amount}
            onChange={(e) => setAmount(e.target.value)} inputProps={{ step: '0.01', min: '0' }} />
          <TextField label="Причина" value={reason} onChange={(e) => setReason(e.target.value)} />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button variant="contained" color="error" onClick={submit} disabled={busy}>Выполнить возврат</Button>
      </DialogActions>
    </Dialog>
  );
}
