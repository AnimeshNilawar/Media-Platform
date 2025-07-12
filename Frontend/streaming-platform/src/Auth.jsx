import React, { useState } from 'react';
import './Auth.css';

const Login = ({ onLogin }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        try {
            const response = await fetch('http://localhost:8765/user/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            let data = {};
            try {
                data = await response.json();
            } catch (jsonErr) {
                console.error('Error parsing JSON:', jsonErr);
                setError('Invalid server response');
                return;
            }
            if (response.ok && data.token) {
                localStorage.setItem('token', data.token);
                onLogin();
            } else {
                setError(data.message || 'Login failed');
            }
        } catch (err) {
            console.error('Network error:', err);
            setError('Network error: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <h2>Login</h2>
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={e => setUsername(e.target.value)}
                    required
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                    required
                />
                <button type="submit" disabled={loading}>{loading ? 'Logging in...' : 'Login'}</button>
                {error && <div className="auth-error">{error}</div>}
            </form>
        </div>
    );
};

const Signup = ({ onSignup }) => {
    const [form, setForm] = useState({
        username: '',
        email: '',
        password: '',
        firstName: '',
        lastName: '',
        profilePictureUrl: '',
        bio: ''
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);
    const [successMsg, setSuccessMsg] = useState('');

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');
        setSuccess(false);
        try {
            const response = await fetch('http://localhost:8765/user/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(form)
            });
            let data = {};
            try {
                data = await response.json();
            } catch (jsonErr) {
                console.error('Error parsing JSON:', jsonErr);
                setError('Invalid server response');
                return;
            }
            if (response.ok) {
                setSuccess(true);
                setSuccessMsg(data.message || 'Signup successful! Please login.');
                onSignup();
            } else {
                setError(data.message || 'Signup failed');
            }
        } catch (err) {
            console.error('Network error:', err);
            setError('Network error: ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">
            <h2>Sign Up</h2>
            <form onSubmit={handleSubmit}>
                <input name="username" placeholder="Username" value={form.username} onChange={handleChange} required />
                <input name="email" type="email" placeholder="Email" value={form.email} onChange={handleChange} required />
                <input name="password" type="password" placeholder="Password" value={form.password} onChange={handleChange} required />
                <input name="firstName" placeholder="First Name" value={form.firstName} onChange={handleChange} required />
                <input name="lastName" placeholder="Last Name" value={form.lastName} onChange={handleChange} required />
                <input name="profilePictureUrl" placeholder="Profile Picture URL" value={form.profilePictureUrl} onChange={handleChange} />
                <textarea name="bio" placeholder="Bio" value={form.bio} onChange={handleChange} />
                <button type="submit" disabled={loading}>{loading ? 'Signing up...' : 'Sign Up'}</button>
                {error && <div className="auth-error">{error}</div>}
                {success && <div className="auth-success">{successMsg}</div>}
            </form>
        </div>
    );
};

export { Login, Signup };
