import { createContext, useCallback, useContext, useState } from 'react';
import { Alert, Snackbar } from '@mui/material';

const FeedbackContext = createContext(null);

export function FeedbackProvider({ children }) {
  const [state, setState] = useState({ open: false, message: '', severity: 'success' });

  const notify = useCallback((message, severity = 'success') => {
    setState({ open: true, message, severity });
  }, []);

  const close = () => setState((s) => ({ ...s, open: false }));

  return (
    <FeedbackContext.Provider value={{ notify }}>
      {children}
      <Snackbar open={state.open} autoHideDuration={5000} onClose={close}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}>
        <Alert onClose={close} severity={state.severity} variant="filled" sx={{ maxWidth: 480 }}>
          {state.message}
        </Alert>
      </Snackbar>
    </FeedbackContext.Provider>
  );
}

export function useFeedback() {
  const ctx = useContext(FeedbackContext);
  if (!ctx) throw new Error('useFeedback must be used within FeedbackProvider');
  return ctx;
}
