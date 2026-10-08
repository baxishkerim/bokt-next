import { useRef, useState } from 'react';
import {
  Alert, Box, Button, Card, CardContent, Chip, Divider, Stack, Table, TableBody,
  TableCell, TableRow, Typography,
} from '@mui/material';
import UploadIcon from '@mui/icons-material/UploadFile';
import PageHeader from '../components/PageHeader.jsx';
import Mono from '../components/Mono.jsx';
import { useFeedback } from '../components/Feedback.jsx';
import { filesApi } from '../api/files.js';
import { errorMessage } from '../api/client.js';

export default function FileImportPage() {
  const { notify } = useFeedback();
  const inputRef = useRef(null);
  const [file, setFile] = useState(null);
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState(null);

  const upload = async () => {
    if (!file) return;
    setBusy(true); setResult(null);
    try {
      const res = await filesApi.upload(file);
      setResult(res);
      notify(`Импорт завершён: ${res.importedRows} из ${res.totalRows}`);
    } catch (e) { notify(errorMessage(e), 'error'); }
    finally { setBusy(false); }
  };

  return (
    <Box>
      <PageHeader title="Импорт кредитов" subtitle="Загрузка заявок из CSV-файла" />

      <Card variant="outlined" sx={{ maxWidth: 640 }}>
        <CardContent>
          <Alert severity="info" sx={{ mb: 2 }}>
            Формат строки: <Mono>pin;clientCardPan;amount;currencyId;description</Mono>.
            Разделитель — точка с запятой, первая строка — заголовок. Клиент ищется по PIN,
            кредиты создаются в статусе «Новый».
          </Alert>

          <Stack direction="row" spacing={2} alignItems="center">
            <input ref={inputRef} type="file" accept=".csv,text/csv" hidden
              onChange={(e) => { setFile(e.target.files?.[0] || null); setResult(null); }} />
            <Button variant="outlined" onClick={() => inputRef.current?.click()}>Выбрать файл</Button>
            <Typography variant="body2" color="text.secondary">
              {file ? file.name : 'Файл не выбран'}
            </Typography>
          </Stack>

          <Button sx={{ mt: 3 }} variant="contained" startIcon={<UploadIcon />}
            disabled={!file || busy} onClick={upload}>
            {busy ? 'Загрузка…' : 'Загрузить'}
          </Button>
        </CardContent>

        {result && (
          <>
            <Divider />
            <CardContent>
              <Typography variant="subtitle2" gutterBottom>Результат</Typography>
              <Table size="small">
                <TableBody>
                  <TableRow><TableCell>Файл</TableCell><TableCell><Mono>{result.fileName}</Mono></TableCell></TableRow>
                  <TableRow><TableCell>Всего строк</TableCell><TableCell><Mono>{result.totalRows}</Mono></TableCell></TableRow>
                  <TableRow><TableCell>Загружено</TableCell><TableCell><Mono>{result.importedRows}</Mono></TableCell></TableRow>
                  <TableRow><TableCell>С ошибками</TableCell><TableCell><Mono>{result.failedRows}</Mono></TableCell></TableRow>
                  <TableRow><TableCell>Статус</TableCell><TableCell><Chip size="small" label={result.status} /></TableCell></TableRow>
                </TableBody>
              </Table>
              {result.errors && (
                <Alert severity="warning" sx={{ mt: 2, whiteSpace: 'pre-line' }}>{result.errors}</Alert>
              )}
            </CardContent>
          </>
        )}
      </Card>
    </Box>
  );
}
