import { Chip } from '@mui/material';
import { CREDIT_STATUS } from '../constants.js';

export default function StatusChip({ status }) {
  const s = CREDIT_STATUS[status] || { label: status, color: 'default' };
  return <Chip size="small" label={s.label} color={s.color} variant="outlined" />;
}
