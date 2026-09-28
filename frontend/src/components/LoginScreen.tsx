import React, { useState } from 'react';

interface LoginScreenProps {
  onLogin: (user: { email: string; name: string; org: string }) => void;
}

export const LoginScreen: React.FC<LoginScreenProps> = ({ onLogin }) => {
  const [email, setEmail] = useState('analyst@ecdat.local');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) return;
    const namePart = email.split('@')[0].replace('.', ' ');
    const formattedName = namePart
      .split(' ')
      .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
      .join(' ');
    onLogin({
      email: email.trim(),
      name: formattedName || 'Security Analyst',
      org: 'Enterprise Cryptographic Operations',
    });
  };

  return (
    <div className="auth-split-page">
      {/* Left Branding Hero Panel */}
      <div className="auth-hero-panel">
        <div className="auth-hero-mesh"></div>
        <div className="auth-hero-content">
          <div className="auth-hero-badge">
            <svg width="42" height="42" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              <path d="m9 12 2 2 4-4" />
            </svg>
          </div>
          <h1 className="auth-hero-title">CRYPTAGUARD</h1>
          <p className="auth-hero-subtitle">
            Enterprise Cryptographic Discovery &amp; Quantum Risk Intelligence
          </p>
          <div className="auth-hero-tagline">
            Discover, Analyze, Secure, Quantum Ready.
          </div>
        </div>
      </div>

      {/* Right Login Form Panel */}
      <div className="auth-form-panel">
        <div className="auth-form-card">
          <div className="auth-card-header">
            <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', marginBottom: '8px' }}>
              <span className="prototype-auth-badge">
                Prototype Authentication · Demo Workspace
              </span>
            </div>
            <h2 className="auth-card-title">Welcome to CRYPTAGUARD</h2>
            <p className="auth-card-subtitle">
              Interactive demonstration workspace session for cryptographic evaluation
            </p>
          </div>

          <form onSubmit={handleSubmit} className="auth-form">
            <div className="form-group">
              <label className="form-label" htmlFor="email-address">
                Email Address / Analyst Handle
              </label>
              <input
                id="email-address"
                type="email"
                className="form-input"
                placeholder="analyst@ecdat.local"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <label className="form-label" htmlFor="password-field">
                  Session Passphrase (Demo Only)
                </label>
                <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>
                  Any value accepted (non-validating)
                </span>
              </div>
              <div className="password-input-wrap">
                <input
                  id="password-field"
                  type={showPassword ? 'text' : 'password'}
                  className="form-input"
                  placeholder="Enter any demo passphrase"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
                <button
                  type="button"
                  className="password-toggle-btn"
                  onClick={() => setShowPassword(!showPassword)}
                  title={showPassword ? 'Hide password' : 'Show password'}
                  aria-label="Toggle password visibility"
                >
                  {showPassword ? (
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                      <line x1="1" y1="1" x2="23" y2="23" />
                    </svg>
                  ) : (
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  )}
                </button>
              </div>
            </div>

            <button type="submit" className="btn-auth-primary">
              Enter Demo Workspace
            </button>
          </form>

          <div className="auth-card-footer">
            <span className="auth-demo-badge">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ marginRight: '4px' }}>
                <circle cx="12" cy="12" r="10" />
                <line x1="12" y1="16" x2="12" y2="12" />
                <line x1="12" y1="8" x2="12.01" y2="8" />
              </svg>
              Prototype Session · Client-side demonstration only · No credentials transmitted or validated
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};
