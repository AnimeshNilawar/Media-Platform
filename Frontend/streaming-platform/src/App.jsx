import React from "react";
import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import VideoUpload from "./VideoUpload";
import VideoPlayer from "./VideoPlayer";
import Navbar from "./Navbar";
import './App.css';

function App() {
  return (
    <Router>
      <Navbar />
      <Routes>
        <Route path="/upload" element={<VideoUpload />} />
        <Route path="/player" element={<VideoPlayer />} />
        <Route path="*" element={<Navigate to="/upload" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
