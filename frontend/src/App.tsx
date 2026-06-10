import { BrowserRouter, Routes, Route } from 'react-router-dom';
import AppLayout from './components/AppLayout';
import Dashboard from './pages/Dashboard';
import ChargingOperationPage from './pages/ChargingOperationPage';
import PileManagementPage from './pages/PileManagementPage';
import BillQueryPage from './pages/BillQueryPage';
import AccountManagementPage from './pages/AccountManagementPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/charging-operation" element={<ChargingOperationPage />} />
          <Route path="/pile-management" element={<PileManagementPage />} />
          <Route path="/bills" element={<BillQueryPage />} />
          <Route path="/account" element={<AccountManagementPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
