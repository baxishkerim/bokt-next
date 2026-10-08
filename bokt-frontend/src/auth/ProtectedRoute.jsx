import { Navigate, useLocation } from 'react-router-dom';
import { Box, Typography } from '@mui/material';
import { useAuth } from './AuthContext.jsx';

export default function ProtectedRoute({ children, anyOf }) {
  const { isAuthenticated, hasAny } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (anyOf && anyOf.length > 0 && !hasAny(anyOf)) {
    return (
      <Box sx={{ p: 4 }}>
        <Typography variant="h6" gutterBottom>Нет доступа</Typography>
        <Typography color="text.secondary">
          Для этого раздела нужны права: {anyOf.join(', ')}.
        </Typography>
      </Box>
    );
  }

  return children;
}
