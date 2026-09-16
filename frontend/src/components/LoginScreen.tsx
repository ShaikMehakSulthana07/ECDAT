import React, { useState } from 'react';

interface LoginScreenProps {
  onLogin: (user: { email: string; name: string; org: string }) => void;
}

export const LoginScreen: React.FC<LoginScreenProps> = ({ onLogin }) => {
  const [email, setEmail] = useState('security.analyst@enterprise.corp');
  const [password, setPassword] = useState('••••••••••••');
  const [rememberMe, setRememberMe] = useState(true);

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
    <div className="auth-page">
      <div className="auth-container">
        <div className="auth-header">
          <div className="auth-logo-badge">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              <path d="m9 12 2 2 4-4" />
            </svg>
          </div>
          <h1 className="auth-title">ECDAT</h1>
          <div className="auth-subtitle-brand">Enterprise Cryptographic Discovery &amp; Analysis</div>
          <div className="auth-welcome">Welcome back</div>
          <p className="auth-instructions">Sign in to access your local analysis workspace.</p>
        </div>

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label className="form-label" htmlFor="work-email">
              Work Email
            </label>
            <input
              id="work-email"
              type="email"
              className="form-input"
              placeholder="name@company.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="work-password">
              Password
            </label>
            <input
              id="work-password"
              type="password"
              className="form-input"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <div className="form-options">
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
              />
              Remember this device
            </label>
            <button
              type="button"
              className="btn-link"
              onClick={() => alert('Password reset is not configured for local application sessions.')}
              style={{ fontSize: '12px', color: 'var(--text-secondary)' }}
            >
              Forgot password?
            </button>
          </div>

          <button type="submit" className="btn-auth-primary">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
              <polyline points="10 17 15 12 10 7" />
              <line x1="15" y1="12" x2="3" y2="12" />
            </svg>
            Sign In
          </button>

          <div className="sso-divider">Single Sign-On (Not Configured)</div>

          <div className="btn-sso-disabled" title="Single Sign-On is not configured in this environment">
            <span>Continue with SSO (Not Configured)</span>
            <span className="sso-subtext">Enterprise SAML 2.0 / OIDC (Unavailable)</span>
          </div>
        </form>

        <div className="auth-footer">
          <span className="auth-footer-notice">Local analysis session</span>
          <span>ECDAT • Cryptographic Security Platform</span>
        </div>
      </div>
    </div>
  );
};
