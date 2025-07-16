import React from "react";
import { Link } from "react-router-dom";
import "./Navbar.css";

const Navbar = ({ onLogout, isAuthenticated }) => {
    const username = localStorage.getItem('username');
    return (
        <nav className="navbar">
            <div className="navbar-logo">Media Platform</div>
            <ul className="navbar-links">
                <li><Link to="/upload">Upload</Link></li>
                <li><Link to="/player">Player</Link></li>
                {isAuthenticated ? (
                    <>
                        {username && <li className="navbar-user">Hello, {username}</li>}
                        <li><button className="navbar-btn" onClick={onLogout}>Logout</button></li>
                    </>
                ) : null}
            </ul>
        </nav>
    );
};

export default Navbar;
