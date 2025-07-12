import React, { useState } from 'react';
import { Login, Signup } from './Auth';
import './Auth.css';

const AuthPage = ({ onAuthSuccess }) => {
    const [showLogin, setShowLogin] = useState(true);

    const handleLogin = () => {
        onAuthSuccess();
    };
    const handleSignup = () => {
        setShowLogin(true);
    };

    return (
        <div className="auth-page-wrapper">
            <div className="auth-toggle">
                <button
                    className={showLogin ? 'active' : ''}
                    onClick={() => setShowLogin(true)}
                >
                    Login
                </button>
                <button
                    className={!showLogin ? 'active' : ''}
                    onClick={() => setShowLogin(false)}
                >
                    Sign Up
                </button>
            </div>
            {showLogin ? (
                <Login onLogin={handleLogin} />
            ) : (
                <Signup onSignup={handleSignup} />
            )}
        </div>
    );
};

export default AuthPage;
