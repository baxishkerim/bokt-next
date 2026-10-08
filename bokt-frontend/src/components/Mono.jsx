import { Box } from '@mui/material';
import { mono } from '../theme.js';

// Моноширинный инлайн-текст для сумм, RRN, GUID, PAN — табличные цифры выравниваются.
export default function Mono({ children, sx }) {
  return (
    <Box component="span" sx={{ fontFamily: mono, fontVariantNumeric: 'tabular-nums', ...sx }}>
      {children}
    </Box>
  );
}
