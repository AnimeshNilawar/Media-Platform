import React, { useState, useEffect } from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import VideoUpload from "./VideoUpload";
import VideoPlayer from "./VideoPlayer";
import Navbar from "./Navbar";
import AuthPage from "./AuthPage";
import './App.css';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('token');
    setIsAuthenticated(!!token);
  }, []);

  const handleAuthSuccess = () => {
    setIsAuthenticated(true);
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    setIsAuthenticated(false);
  };

  return (
    <Router>
      <Navbar onLogout={handleLogout} isAuthenticated={isAuthenticated} />
      {isAuthenticated ? (
        <Routes>
          <Route path="/upload" element={<VideoUpload />} />
          <Route path="/player" element={<VideoPlayer />} />
          <Route path="*" element={<Navigate to="/upload" replace />} />
        </Routes>
      ) : (
        <AuthPage onAuthSuccess={handleAuthSuccess} />
      )}
    </Router>
  );
}

export default App;
