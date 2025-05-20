import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import './App.css';
import MainPage from './pages/main-page/main-page';
import LoginPage from './pages/login-page/login-page';
import AdminRoute from './utils/admin-route/admin-route';

function App() {
  return (
    <Router>
      <Routes>
        <Route element={<AdminRoute />}>
          <Route path="/" element={<MainPage />} />
        </Route>
        <Route path="/login" element={<LoginPage />} />
      </Routes>
    </Router>
  );
}

export default App;
