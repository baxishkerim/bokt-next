import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import ProtectedRoute from './auth/ProtectedRoute.jsx';
import { P } from './constants.js';

import LoginPage from './pages/LoginPage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import CreditsPage from './pages/CreditsPage.jsx';
import ClientsPage from './pages/ClientsPage.jsx';
import FileImportPage from './pages/FileImportPage.jsx';
import UsersPage from './pages/UsersPage.jsx';
import RolesPage from './pages/RolesPage.jsx';
import BranchesPage from './pages/BranchesPage.jsx';
import OrganisationsPage from './pages/OrganisationsPage.jsx';
import CardsPage from './pages/CardsPage.jsx';
import AuditPage from './pages/AuditPage.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<DashboardPage />} />
        <Route path="credits" element={
          <ProtectedRoute anyOf={[P.CREDIT_VIEW, P.CREDIT_ADD]}><CreditsPage /></ProtectedRoute>} />
        <Route path="clients" element={
          <ProtectedRoute anyOf={[P.CLIENT_MANAGE, P.CREDIT_VIEW, P.CREDIT_ADD]}><ClientsPage /></ProtectedRoute>} />
        <Route path="import" element={
          <ProtectedRoute anyOf={[P.FILE_IMPORT]}><FileImportPage /></ProtectedRoute>} />
        <Route path="users" element={
          <ProtectedRoute anyOf={[P.USER_MANAGE]}><UsersPage /></ProtectedRoute>} />
        <Route path="roles" element={
          <ProtectedRoute anyOf={[P.ROLE_MANAGE]}><RolesPage /></ProtectedRoute>} />
        <Route path="branches" element={<BranchesPage />} />
        <Route path="cards" element={
          <ProtectedRoute anyOf={[P.CARD_MANAGE]}><CardsPage /></ProtectedRoute>} />
        <Route path="organisations" element={
          <ProtectedRoute anyOf={[P.TENANT_MANAGE]}><OrganisationsPage /></ProtectedRoute>} />
        <Route path="audit" element={
          <ProtectedRoute anyOf={[P.REPORT_VIEW]}><AuditPage /></ProtectedRoute>} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
