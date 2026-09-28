import React, { useState } from 'react';
import { apiService } from '../services/api';

interface SettingsViewProps {
  user: { name: string; email: string; org: string };
  backendStatus: 'UP' | 'DOWN' | 'CHECKING';
  onHealthCheck: () => void;
  theme?: 'light' | 'dark';
  onToggleTheme?: () => void;
  onLogout?: () => void;
}

export const SettingsView: React.FC<SettingsViewProps> = ({
  user,
  backendStatus,
  onHealthCheck,
  theme = 'dark',
  onToggleTheme,
  onLogout,
}) => {
  const [activeTab, setActiveTab] = useState<'account' | 'security' | 'notifications' | 'system'>('account');

  return (
    <div>
      {/* Header matching Screen 11 */}
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Settings</h1>
          <p className="view-subtitle">Application configuration and account information</p>
        </div>
      </div>

      <div className="settings-split-grid">
        {/* Left Tabs */}
        <div className="card-panel settings-nav-panel">
          <button
            className={`settings-nav-btn ${activeTab === 'account' ? 'active' : ''}`}
            onClick={() => setActiveTab('account')}
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
            Account
          </button>
          <button
            className={`settings-nav-btn ${activeTab === 'security' ? 'active' : ''}`}
            onClick={() => setActiveTab('security')}
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
              <path d="M7 11V7a5 5 0 0 1 10 0v4" />
            </svg>
            Security &amp; Appearance
          </button>
          <button
            className={`settings-nav-btn ${activeTab === 'notifications' ? 'active' : ''}`}
            onClick={() => setActiveTab('notifications')}
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
              <path d="M13.73 21a2 2 0 0 1-3.46 0" />
            </svg>
            Notifications
          </button>
          <button
            className={`settings-nav-btn ${activeTab === 'system' ? 'active' : ''}`}
            onClick={() => setActiveTab('system')}
          >
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="3" />
              <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
            </svg>
            System
          </button>
        </div>

        {/* Right Content */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {activeTab === 'account' && (
            <div className="card-panel">
              <h3 className="card-panel-title" style={{ marginBottom: '16px' }}>
                Account Information
              </h3>
              <table className="props-table">
                <tbody>
                  <tr>
                    <th>Name</th>
                    <td className="font-bold">{user.name}</td>
                  </tr>
                  <tr>
                    <th>Email</th>
                    <td className="font-mono">{user.email}</td>
                  </tr>
                  <tr>
                    <th>Role</th>
                    <td>
                      <span className="tag-subtle font-mono">SECURITY_ANALYST</span>
                    </td>
                  </tr>
                  <tr>
                    <th>Session Mode</th>
                    <td>
                      <span className="risk-badge low">
                        Prototype Authentication · Demo Workspace (ACTIVE)
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <th>Auth Mechanism</th>
                    <td>
                      <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                        Client-side demonstration session stored in browser <code>localStorage</code>. No backend authentication or JWT verification is performed in prototype mode.
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>

              {onLogout && (
                <div style={{ marginTop: '20px', display: 'flex', justifyContent: 'flex-start' }}>
                  <button className="btn-secondary" onClick={onLogout}>
                    Logout
                  </button>
                </div>
              )}
            </div>
          )}

          {activeTab === 'security' && (
            <div className="card-panel">
              <div className="card-panel-header">
                <div>
                  <h3 className="card-panel-title">Platform Appearance &amp; Theme</h3>
                  <span className="card-panel-sub">Configure visual display mode for enterprise workspace</span>
                </div>
                {onToggleTheme && (
                  <button className="btn-secondary btn-sm" onClick={onToggleTheme}>
                    Toggle to {theme === 'dark' ? 'Light Theme' : 'Dark Theme'}
                  </button>
                )}
              </div>
              <table className="props-table">
                <tbody>
                  <tr>
                    <th>Active Theme</th>
                    <td>
                      <span className="tag-subtle font-mono">
                        {theme === 'dark' ? 'Dark Theme (Enterprise Slate)' : 'Light Theme (Enterprise Crisp)'}
                      </span>
                    </td>
                  </tr>
                  <tr>
                    <th>Theme Persistence</th>
                    <td>
                      <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>
                        Saved automatically in local browser storage (<code>ecdat_theme</code>).
                      </span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          )}

          {activeTab === 'notifications' && (
            <div className="card-panel">
              <h3 className="card-panel-title" style={{ marginBottom: '16px' }}>
                Notification Preferences
              </h3>
              <p style={{ fontSize: '12px', color: 'var(--text-secondary)', marginBottom: '14px' }}>
                Manage how cryptographic discovery alerts and quantum exposure warnings are delivered.
              </p>
              <table className="props-table">
                <tbody>
                  <tr>
                    <th>Critical Quantum Threat Alerts</th>
                    <td><span className="risk-badge low">ENABLED (IN-APP)</span></td>
                  </tr>
                  <tr>
                    <th>NIST FIPS 203/204/205 Standards Updates</th>
                    <td><span className="risk-badge low">ENABLED (IN-APP)</span></td>
                  </tr>
                </tbody>
              </table>
            </div>
          )}

          {activeTab === 'system' && (
            <>
              {/* Backend Engine & Health */}
              <div className="card-panel">
                <div className="card-panel-header">
                  <div>
                    <h3 className="card-panel-title">Cryptographic Discovery Engine API</h3>
                    <span className="card-panel-sub">Spring Boot REST service connection status</span>
                  </div>
                  <button className="btn-secondary btn-sm" onClick={onHealthCheck}>
                    Re-check Health
                  </button>
                </div>

                <table className="props-table">
                  <tbody>
                    <tr>
                      <th>Endpoint URL</th>
                      <td className="font-mono">{apiService.getBaseUrl()}</td>
                    </tr>
                    <tr>
                      <th>Service Status</th>
                      <td>
                        <span className={`risk-badge ${backendStatus === 'UP' ? 'low' : 'critical'}`}>
                          {backendStatus === 'UP' ? 'ONLINE (HEALTHY)' : backendStatus === 'CHECKING' ? 'CHECKING...' : 'OFFLINE'}
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <th>Active Rulesets</th>
                      <td>
                        <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                          <span className="tag-subtle font-mono">NIST FIPS 203 (ML-KEM)</span>
                          <span className="tag-subtle font-mono">NIST FIPS 204 (ML-DSA)</span>
                          <span className="tag-subtle font-mono">NIST FIPS 205 (SLH-DSA)</span>
                          <span className="tag-subtle font-mono">Java AST Static Engine</span>
                          <span className="tag-subtle font-mono">X.509 Certificate Parser</span>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              {/* Security & Sandbox Policies */}
              <div className="card-panel">
                <h3 className="card-panel-title" style={{ marginBottom: '16px' }}>
                  Archive Extraction &amp; Security Controls
                </h3>
                <table className="props-table">
                  <tbody>
                    <tr>
                      <th>Archive Isolation</th>
                      <td>
                        <span className="tag-subtle" style={{ color: 'var(--risk-low)' }}>
                          Isolated sandboxed temporary directory per upload
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <th>Path Traversal Protection</th>
                      <td>
                        <span className="tag-subtle font-mono">Zip Slip Canonicalization Protection Active</span>
                      </td>
                    </tr>
                    <tr>
                      <th>Archive Limits</th>
                      <td className="font-mono">
                        Max 10,000 entries · 200 MB maximum uncompressed limit
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};
