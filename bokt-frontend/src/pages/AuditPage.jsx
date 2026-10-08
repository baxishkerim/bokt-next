import { useState } from 'react';
import {
  Box, Button, MenuItem, Paper, Stack, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, TextField, Typography,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import PageHeader from '../components/PageHeader.jsx';
import Mono from '../components/Mono.jsx';
import { useFeedback } from '../components/Feedback.jsx';
import { auditApi } from '../api/audit.js';
import { errorMessage } from '../api/client.js';

const ENTITY_TYPES = ['credit', 'client', 'user', 'organisation'];

export default function AuditPage() {
  const { notify } = useFeedback();
  const [mode, setMode] = useState('correlation'); // 'correlation' | 'entity'
  const [correlationId, setCorrelationId] = useState('');
  const [entityType, setEntityType] = useState('credit');
  const [entityId, setEntityId] = useState('');
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(false);

  const search = async () => {
    setLoading(true);
    try {
      const data = mode === 'correlation'
        ? await auditApi.byCorrelation(correlationId.trim())
        : await auditApi.byEntity(entityType, entityId.trim());
      setRows(data);
      if (data.length === 0) notify('Событий не найдено', 'info');
    } catch (e) { notify(errorMessage(e), 'error'); }
    finally { setLoading(false); }
  };

  return (
    <Box>
      <PageHeader title="Аудит" subtitle="Трассировка операций по correlationId или объекту" />

      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ mb: 2 }} alignItems="flex-start">
        <TextField select label="Искать по" value={mode} size="small" sx={{ minWidth: 200 }}
          onChange={(e) => { setMode(e.target.value); setRows([]); }}>
          <MenuItem value="correlation">Correlation ID</MenuItem>
          <MenuItem value="entity">Объекту</MenuItem>
        </TextField>

        {mode === 'correlation' ? (
          <TextField label="Correlation ID" value={correlationId} size="small" sx={{ minWidth: 320 }}
            onChange={(e) => setCorrelationId(e.target.value)}
            helperText="Из заголовка X-Correlation-Id любого ответа" />
        ) : (
          <>
            <TextField select label="Тип" value={entityType} size="small" sx={{ minWidth: 160 }}
              onChange={(e) => setEntityType(e.target.value)}>
              {ENTITY_TYPES.map((t) => <MenuItem key={t} value={t}>{t}</MenuItem>)}
            </TextField>
            <TextField label="ID объекта" value={entityId} size="small" sx={{ minWidth: 160 }}
              onChange={(e) => setEntityId(e.target.value)} />
          </>
        )}

        <Button variant="contained" startIcon={<SearchIcon />} onClick={search}
          sx={{ mt: { sm: 0.25 } }}>Искать</Button>
      </Stack>

      <TableContainer component={Paper} variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Время</TableCell><TableCell>Действие</TableCell><TableCell>Объект</TableCell>
              <TableCell>Пользователь</TableCell><TableCell>Детали</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rows.map((e) => (
              <TableRow key={e.id} hover>
                <TableCell><Mono sx={{ fontSize: 12 }}>{e.createdAt}</Mono></TableCell>
                <TableCell>{e.action}</TableCell>
                <TableCell><Mono>{e.entityType}{e.entityId ? `#${e.entityId}` : ''}</Mono></TableCell>
                <TableCell><Mono>{e.userId ?? '—'}</Mono></TableCell>
                <TableCell>{e.details || '—'}</TableCell>
              </TableRow>
            ))}
            {rows.length === 0 && (
              <TableRow><TableCell colSpan={5}>
                <Typography color="text.secondary" sx={{ py: 3, textAlign: 'center' }}>
                  {loading ? 'Поиск…' : 'Задайте параметры и нажмите «Искать».'}
                </Typography></TableCell></TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
