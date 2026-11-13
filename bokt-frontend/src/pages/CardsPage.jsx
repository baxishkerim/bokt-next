import { useCallback, useEffect, useState } from 'react';
import {
  Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, IconButton,
  Paper, Stack, Table, TableBody, TableCell, TableContainer, TableHead,
  TableRow, TextField, Tooltip, Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import BlockIcon from '@mui/icons-material/Block';
import CheckIcon from '@mui/icons-material/CheckCircle';
import EditIcon from '@mui/icons-material/Edit';
import PageHeader from '../components/PageHeader.jsx';
import Mono from '../components/Mono.jsx';
import { useFeedback } from '../components/Feedback.jsx';
import { cardsApi } from '../api/cards.js';
import { errorMessage } from '../api/client.js';

export default function CardsPage() {
  const { notify } = useFeedback();
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);
  const [addOpen, setAddOpen] = useState(false);
  const [balanceCard, setBalanceCard] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try { setRows(await cardsApi.list()); }
    catch (e) { notify(errorMessage(e), 'error'); }
    finally { setLoading(false); }
  }, [notify]);

  useEffect(() => { load(); }, [load]);

  const act = async (fn, msg) => {
    try { await fn(); notify(msg); load(); } catch (e) { notify(errorMessage(e), 'error'); }
  };

  return (
    <Box>
      <PageHeader title="Материнская карта организации"
        subtitle="Одна карта-источник на организацию (с неё списывается при выдаче). Карта клиента задаётся в заявке на кредит."
        action={<Button variant="contained" startIcon={<AddIcon />} onClick={() => setAddOpen(true)}>
          {rows.length > 0 ? 'Изменить карту' : 'Добавить карту'}</Button>} />

      <TableContainer component={Paper} variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>ID</TableCell><TableCell>Карта</TableCell>
              <TableCell>Срок</TableCell><TableCell align="right">Остаток</TableCell>
              <TableCell>Статус</TableCell><TableCell align="right">Действия</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((c) => (
              <TableRow key={c.id} hover>
                <TableCell><Mono>{c.id}</Mono></TableCell>
                <TableCell><Mono>{c.maskedPan}</Mono></TableCell>
                <TableCell><Mono>{c.expiry || '—'}</Mono></TableCell>
                <TableCell align="right"><Mono sx={{ fontWeight: 600 }}>{Number(c.balance).toFixed(2)}</Mono></TableCell>
                <TableCell>
                  <Chip size="small" variant="outlined" color={c.status === 'ACTIVE' ? 'success' : 'default'}
                    label={c.status === 'ACTIVE' ? 'Активна' : 'Заблокирована'} />
                </TableCell>
                <TableCell align="right">
                  <Tooltip title="Изменить остаток"><IconButton size="small" onClick={() => setBalanceCard(c)}>
                    <EditIcon fontSize="small" /></IconButton></Tooltip>
                  {c.status === 'ACTIVE' ? (
                    <Tooltip title="Заблокировать"><IconButton size="small" color="error"
                      onClick={() => act(() => cardsApi.block(c.id), 'Карта заблокирована')}>
                      <BlockIcon fontSize="small" /></IconButton></Tooltip>
                  ) : (
                    <Tooltip title="Разблокировать"><IconButton size="small" color="success"
                      onClick={() => act(() => cardsApi.unblock(c.id), 'Карта разблокирована')}>
                      <CheckIcon fontSize="small" /></IconButton></Tooltip>
                  )}
                </TableCell>
              </TableRow>
            ))}
            {rows.length === 0 && (
              <TableRow><TableCell colSpan={6}>
                <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                  {loading ? 'Загрузка…' : 'Карт нет. Добавьте карту-источник организации.'}
                </Typography></TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {addOpen && (
        <AddCardDialog card={rows[0] || null} onClose={() => setAddOpen(false)}
          onAdded={() => { setAddOpen(false); load(); notify(rows.length > 0 ? 'Карта обновлена' : 'Карта добавлена'); }} />
      )}
      {balanceCard && (
        <BalanceDialog card={balanceCard} onClose={() => setBalanceCard(null)}
          onSaved={() => { setBalanceCard(null); load(); notify('Остаток обновлён'); }} />
      )}
    </Box>
  );
}

function AddCardDialog({ card, onClose, onAdded }) {
  const { notify } = useFeedback();
  const editing = Boolean(card);
  const [form, setForm] = useState({
    pan: '', expiry: card?.expiry || '', cvv: '', merchant: card?.merchant || '', balance: '',
  });
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  const submit = async () => {
    setBusy(true);
    try {
      const body = {
        pan: form.pan.trim() || null, expiry: form.expiry || null,
        cvv: form.cvv.trim() || null, merchant: form.merchant || null,
      };
      if (!editing) body.balance = form.balance ? Number(form.balance) : 0;
      await cardsApi.add(body);
      onAdded();
    } catch (e) { notify(errorMessage(e), 'error'); }
    finally { setBusy(false); }
  };

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>{editing ? 'Изменить материнскую карту' : 'Материнская карта организации'}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField label="Номер карты (PAN)" value={form.pan} onChange={set('pan')}
            helperText={editing ? `Текущая: ${card.maskedPan}. Оставьте пустым, чтобы не менять` : undefined} />
          <TextField label="Срок (MMYY)" value={form.expiry} onChange={set('expiry')} />
          <TextField label="CVV" value={form.cvv} onChange={set('cvv')}
            helperText={editing ? 'Оставьте пустым, чтобы не менять' : 'Нужен для выдачи (cardToCard)'} />
          <TextField label="Мерчант / терминал" value={form.merchant} onChange={set('merchant')} />
          {!editing && (
            <TextField label="Остаток (AZN, инфо)" type="number" value={form.balance}
              onChange={set('balance')} inputProps={{ step: '0.01', min: '0' }} />
          )}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button variant="contained" onClick={submit} disabled={busy || (!editing && !form.pan)}>
          {editing ? 'Сохранить' : 'Добавить'}</Button>
      </DialogActions>
    </Dialog>
  );
}

function BalanceDialog({ card, onClose, onSaved }) {
  const { notify } = useFeedback();
  const [balance, setBalance] = useState(String(card.balance ?? ''));
  const [busy, setBusy] = useState(false);

  const submit = async () => {
    setBusy(true);
    try { await cardsApi.setBalance(card.id, Number(balance)); onSaved(); }
    catch (e) { notify(errorMessage(e), 'error'); }
    finally { setBusy(false); }
  };

  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>Остаток карты #{card.id}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <Typography variant="body2" color="text.secondary">
            Информационный остаток карты организации (факт подтягивается из процессинга).
          </Typography>
          <TextField label="Остаток (AZN)" type="number" value={balance}
            onChange={(e) => setBalance(e.target.value)} inputProps={{ step: '0.01', min: '0' }} autoFocus />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button variant="contained" onClick={submit} disabled={busy}>Сохранить</Button>
      </DialogActions>
    </Dialog>
  );
}
