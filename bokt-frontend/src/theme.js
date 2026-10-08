import { createTheme } from '@mui/material/styles';

// Консоль операций мгновенного кредитования: чернильно-синий как основа доверия,
// сдержанный зелёный — «деньги/подтверждение», статусы — отдельной палитрой.
// Моноширинный шрифт и табличные цифры — для сумм, RRN, GUID, PAN.
const ink = '#16324F';
const money = '#0E8A6B';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: ink, dark: '#0E2438', light: '#375A79' },
    secondary: { main: money, dark: '#0A6B53' },
    background: { default: '#F5F7FA', paper: '#FFFFFF' },
    text: { primary: '#1A2733', secondary: '#5A6B7B' },
    success: { main: money },
    warning: { main: '#B7791F' },
    error: { main: '#C0392B' },
    info: { main: '#375A79' },
    divider: '#E1E7EE',
  },
  shape: { borderRadius: 8 },
  typography: {
    fontFamily: 'Inter, system-ui, Arial, sans-serif',
    h5: { fontWeight: 700, letterSpacing: '-0.01em' },
    h6: { fontWeight: 700, letterSpacing: '-0.01em' },
    subtitle2: { fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600 },
    // класс для денег/идентификаторов используем через sx с этим семейством
    fontFamilyMono: '"IBM Plex Mono", ui-monospace, SFMono-Regular, Menlo, monospace',
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        // табличные цифры по всему интерфейсу — суммы и коды выравниваются столбцом
        body: { fontVariantNumeric: 'tabular-nums' },
      },
    },
    MuiPaper: { styleOverrides: { root: { backgroundImage: 'none' } } },
    MuiButton: { defaultProps: { disableElevation: true } },
    MuiTableCell: {
      styleOverrides: {
        head: { fontWeight: 600, color: '#5A6B7B', backgroundColor: '#FAFBFC' },
      },
    },
    MuiAppBar: {
      styleOverrides: { root: { backgroundColor: ink } },
    },
  },
});

// удобный доступ к моноширинному семейству
export const mono = '"IBM Plex Mono", ui-monospace, SFMono-Regular, Menlo, monospace';

export default theme;
