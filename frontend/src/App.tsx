import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './components/AuthContext';
import AppLayout from './components/AppLayout';
import LoginPage from './pages/LoginPage';
import Dashboard from './pages/Dashboard';
import ChargingOperationPage from './pages/ChargingOperationPage';
import StatusQueryPage from './pages/StatusQueryPage';
import MyBillsPage from './pages/MyBillsPage';
import PileManagementPage from './pages/PileManagementPage';
import BillQueryPage from './pages/BillQueryPage';

function ProtectedRoute({ children, requiredRole }: { children: React.ReactNode; requiredRole?: 'user' | 'admin' }) {
  const { role } = useAuth();
  if (role === 'none') return <Navigate to="/login" replace />;
  if (requiredRole && role !== requiredRole) return <Navigate to="/" replace />;
  return <>{children}</>;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute><AppLayout /></ProtectedRoute>}>
        <Route path="/" element={<ProtectedRoute requiredRole="user"><Dashboard /></ProtectedRoute>} />
        <Route path="/charging-operation" element={<ProtectedRoute requiredRole="user"><ChargingOperationPage /></ProtectedRoute>} />
        <Route path="/status-query" element={<ProtectedRoute requiredRole="user"><StatusQueryPage /></ProtectedRoute>} />
        <Route path="/my-bills" element={<ProtectedRoute requiredRole="user"><MyBillsPage /></ProtectedRoute>} />
        <Route path="/admin" element={<ProtectedRoute requiredRole="admin"><Dashboard /></ProtectedRoute>} />
        <Route path="/bills" element={<ProtectedRoute requiredRole="admin"><BillQueryPage /></ProtectedRoute>} />
        <Route path="/pile-management" element={<ProtectedRoute requiredRole="admin"><PileManagementPage /></ProtectedRoute>} />
      </Route>
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
