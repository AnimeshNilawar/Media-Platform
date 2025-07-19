import React, { useState } from 'react';
import { Login, Signup } from './Auth';

const AuthPage = ({ onAuthSuccess }) => {
    const [showLogin, setShowLogin] = useState(true);

    const handleLogin = () => {
        onAuthSuccess();
    };
    
    const handleSignup = () => {
        setShowLogin(true);
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900 flex items-center justify-center p-4 relative overflow-hidden">
            {/* Animated Background Blobs */}
            <div className="absolute inset-0 overflow-hidden">
                <div className="absolute -top-40 -right-40 w-80 h-80 bg-purple-500 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-pulse"></div>
                <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-blue-500 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-pulse" style={{animationDelay: '1s'}}></div>
                <div className="absolute top-40 left-40 w-80 h-80 bg-pink-500 rounded-full mix-blend-multiply filter blur-xl opacity-70 animate-pulse" style={{animationDelay: '2s'}}></div>
            </div>
            
            <div className="relative z-10 w-full max-w-md">
                {/* Toggle Buttons */}
                <div className="flex mb-8 bg-white/5 backdrop-blur-sm rounded-2xl p-1 border border-white/10">
                    <button
                        className={`flex-1 py-3 px-6 rounded-xl font-semibold transition-all duration-300 ${
                            showLogin
                                ? 'bg-gradient-to-r from-blue-500 to-purple-600 text-white shadow-lg transform scale-[1.02]'
                                : 'text-white/70 hover:text-white hover:bg-white/5'
                        }`}
                        onClick={() => setShowLogin(true)}
                    >
                        Login
                    </button>
                    <button
                        className={`flex-1 py-3 px-6 rounded-xl font-semibold transition-all duration-300 ${
                            !showLogin
                                ? 'bg-gradient-to-r from-purple-500 to-pink-600 text-white shadow-lg transform scale-[1.02]'
                                : 'text-white/70 hover:text-white hover:bg-white/5'
                        }`}
                        onClick={() => setShowLogin(false)}
                    >
                        Sign Up
                    </button>
                </div>
                
                {/* Auth Components */}
                <div className="transition-all duration-500 ease-in-out">
                    {showLogin ? (
                        <Login onLogin={handleLogin} />
                    ) : (
                        <Signup onSignup={handleSignup} />
                    )}
                </div>
            </div>
        </div>
    );
};

export default AuthPage;