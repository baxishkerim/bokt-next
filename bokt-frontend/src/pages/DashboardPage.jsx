import { useEffect, useState } from 'react';
import { Link as RouterLink } from 'react-router-dom';
import { Box, Button, Card, CardContent, Chip, Grid, Stack, Typography } from '@mui/material';
import PageHeader from '../components/PageHeader.jsx';
import { useAuth } from '../auth/AuthContext.jsx';
import { P } from '../constants.js';
import { creditsApi } from '../api/credits.js';
import { errorMessage } from '../api/client.js';

function Tile({ label, value, to, cta }) {
  return (
    <Card sx={{ border: '1px solid', borderColor: 'divider', height: '100%' }}>
      <CardContent>
        <Typography variant="overline" color="text.secondary">{label}</Typography>
        <Typography variant="h4" sx={{ my: 1, fontVariantNumeric: 'tabular-nums' }}>{value}</Typography>
        {to && <Button component={RouterLink} to={to} size="small">{cta || 'Открыть'}</Button>}
      </CardContent>
    </Card>
  );
}

export default function DashboardPage() {
  const { user, hasAny } = useAuth();
  const [counts, setCounts] = useState({ new: '—', approved: '—' });

  useEffect(() => {
    if (!hasAny([P.CREDIT_VIEW])) return;
    Promise.all([creditsApi.list('NEW', 0, 1), creditsApi.list('APPROVED', 0, 1)])
      .then(([n, a]) => setCounts({ new: n.totalElements, approved: a.totalElements }))
      .catch((e) => console.warn(errorMessage(e)));
  }, [hasAny]);

  return (
    <Box>
      <PageHeader
        title={`Здравствуйте, ${user?.username}`}
        subtitle="Консоль операций мгновенного кредитования"
      />

      <Grid container spacing={2} sx={{ mb: 3 }}>
        {hasAny([P.CREDIT_VIEW]) && (
          <>
            <Grid item xs={12} sm={6} md={3}>
              <Tile label="Новые заявки" value={counts.new} to="/credits" cta="К кредитам" />
            </Grid>
            <Grid item xs={12} sm={6} md={3}>
              <Tile label="Подтверждённые" value={counts.approved} to="/credits" />
            </Grid>
          </>
        )}
        {hasAny([P.CREDIT_ADD]) && (
          <Grid item xs={12} sm={6} md={3}>
            <Tile label="Действие" value="+" to="/credits" cta="Новая заявка" />
          </Grid>
        )}
      </Grid>

      <Card sx={{ border: '1px solid', borderColor: 'divider' }}>
        <CardContent>
          <Typography variant="subtitle2" gutterBottom>Ваши права</Typography>
          {user?.superAdmin ? (
            <Chip label="Полный доступ (супер-админ)" color="secondary" />
          ) : (
            <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
              {(user?.authorities || []).map((a) => (
                <Chip key={a} label={a} size="small" variant="outlined" />
              ))}
              {(!user?.authorities || user.authorities.length === 0) && (
                <Typography color="text.secondary">Права не назначены.</Typography>
              )}
            </Stack>
          )}
        </CardContent>
      </Card>
    </Box>
  );
}
