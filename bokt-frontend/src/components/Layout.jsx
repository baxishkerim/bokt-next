import { useState } from 'react';
import { Link as RouterLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import {
  AppBar, Avatar, Box, Chip, Divider, Drawer, IconButton, List, ListItemButton,
  ListItemIcon, ListItemText, Toolbar, Tooltip, Typography, useMediaQuery,
} from '@mui/material';
import { useTheme } from '@mui/material/styles';
import MenuIcon from '@mui/icons-material/Menu';
import LogoutIcon from '@mui/icons-material/Logout';
import DashboardIcon from '@mui/icons-material/SpaceDashboard';
import CreditIcon from '@mui/icons-material/RequestQuote';
import PeopleIcon from '@mui/icons-material/People';
import UploadIcon from '@mui/icons-material/UploadFile';
import BadgeIcon from '@mui/icons-material/Badge';
import SecurityIcon from '@mui/icons-material/AdminPanelSettings';
import StoreIcon from '@mui/icons-material/AccountBalance';
import ApartmentIcon from '@mui/icons-material/Apartment';
import CreditCardIcon from '@mui/icons-material/CreditCard';
import HistoryIcon from '@mui/icons-material/ManageSearch';
import { useAuth } from '../auth/AuthContext.jsx';
import { P } from '../constants.js';

const WIDTH = 248;

const NAV = [
  { to: '/', label: 'Обзор', icon: <DashboardIcon />, anyOf: null },
  { to: '/credits', label: 'Кредиты', icon: <CreditIcon />, anyOf: [P.CREDIT_VIEW, P.CREDIT_ADD] },
  { to: '/clients', label: 'Клиенты', icon: <PeopleIcon />, anyOf: [P.CLIENT_MANAGE, P.CREDIT_VIEW, P.CREDIT_ADD] },
  { to: '/import', label: 'Импорт кредитов', icon: <UploadIcon />, anyOf: [P.FILE_IMPORT] },
  { to: '/users', label: 'Пользователи', icon: <BadgeIcon />, anyOf: [P.USER_MANAGE] },
  { to: '/roles', label: 'Роли', icon: <SecurityIcon />, anyOf: [P.ROLE_MANAGE] },
  { to: '/branches', label: 'Филиалы', icon: <ApartmentIcon />, anyOf: null },
  { to: '/cards', label: 'Карты организации', icon: <CreditCardIcon />, anyOf: [P.CARD_MANAGE] },
  { to: '/organisations', label: 'Организации (NBCO)', icon: <StoreIcon />, anyOf: [P.TENANT_MANAGE] },
  { to: '/audit', label: 'Аудит', icon: <HistoryIcon />, anyOf: [P.REPORT_VIEW] },
];

export default function Layout() {
  const theme = useTheme();
  const isDesktop = useMediaQuery(theme.breakpoints.up('md'));
  const [open, setOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { user, hasAny, logout } = useAuth();

  const visible = NAV.filter((n) => !n.anyOf || hasAny(n.anyOf));

  const drawer = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Toolbar sx={{ px: 2 }}>
        <Box>
          <Typography variant="h6" sx={{ color: 'primary.main', lineHeight: 1 }}>BOKT<span style={{ color: '#0E8A6B' }}>·</span>next</Typography>
          <Typography variant="caption" color="text.secondary">консоль операций</Typography>
        </Box>
      </Toolbar>
      <Divider />
      <List sx={{ px: 1, py: 1, flexGrow: 1 }}>
        {visible.map((n) => {
          const selected = n.to === '/' ? location.pathname === '/' : location.pathname.startsWith(n.to);
          return (
            <ListItemButton
              key={n.to}
              component={RouterLink}
              to={n.to}
              selected={selected}
              onClick={() => !isDesktop && setOpen(false)}
              sx={{ borderRadius: 2, mb: 0.5 }}
            >
              <ListItemIcon sx={{ minWidth: 40, color: selected ? 'primary.main' : 'text.secondary' }}>
                {n.icon}
              </ListItemIcon>
              <ListItemText primaryTypographyProps={{ fontWeight: selected ? 600 : 500 }} primary={n.label} />
            </ListItemButton>
          );
        })}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: 'flex', minHeight: '100%' }}>
      <AppBar position="fixed" sx={{ zIndex: (t) => t.zIndex.drawer + 1 }}>
        <Toolbar>
          {!isDesktop && (
            <IconButton color="inherit" edge="start" onClick={() => setOpen(true)} sx={{ mr: 1 }}>
              <MenuIcon />
            </IconButton>
          )}
          <Box sx={{ flexGrow: 1 }} />
          {user?.superAdmin && (
            <Chip size="small" label="СУПЕР-АДМИН" color="secondary"
                  sx={{ mr: 2, fontWeight: 600 }} />
          )}
          <Box sx={{ textAlign: 'right', mr: 1.5, display: { xs: 'none', sm: 'block' } }}>
            <Typography variant="body2" sx={{ lineHeight: 1.1 }}>{user?.username}</Typography>
            <Typography variant="caption" sx={{ opacity: 0.7 }}>тенант #{user?.tenantId ?? '—'}</Typography>
          </Box>
          <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main', fontSize: 14 }}>
            {(user?.username || '?').slice(0, 1).toUpperCase()}
          </Avatar>
          <Tooltip title="Выйти">
            <IconButton color="inherit" onClick={async () => { await logout(); navigate('/login'); }} sx={{ ml: 1 }}>
              <LogoutIcon />
            </IconButton>
          </Tooltip>
        </Toolbar>
      </AppBar>

      {/* Постоянный drawer на десктопе, временный на мобильном */}
      {isDesktop ? (
        <Drawer variant="permanent" sx={{ width: WIDTH, flexShrink: 0,
          '& .MuiDrawer-paper': { width: WIDTH, boxSizing: 'border-box' } }} open>
          {drawer}
        </Drawer>
      ) : (
        <Drawer variant="temporary" open={open} onClose={() => setOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{ '& .MuiDrawer-paper': { width: WIDTH, boxSizing: 'border-box' } }}>
          {drawer}
        </Drawer>
      )}

      <Box component="main" sx={{ flexGrow: 1, p: { xs: 2, md: 4 }, width: { md: `calc(100% - ${WIDTH}px)` } }}>
        <Toolbar />
        <Outlet />
      </Box>
    </Box>
  );
}
