import React from "react";
import { Link } from "react-router-dom";
import "./App.css";

const Navbar = () => (
    <nav className="navbar">
        <ul>
            <li><Link to="/upload">Upload Video</Link></li>
            <li><Link to="/player">Video Player</Link></li>
        </ul>
    </nav>
);

export default Navbar;
