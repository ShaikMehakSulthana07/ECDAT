import React, { useState } from 'react';
import type { NavigationPage } from './Sidebar';

interface TopBarProps {
  activePage: NavigationPage;
  backendStatus: 'UP' | 'DOWN' | 'CHECKING';
  searchQuery: string;
  onSearchChange: (query: string) => void;
  targetPath?: string;
  theme?: 'light' | 'dark';
  onToggleTheme?: () => void;
  user?: { name: string; email: string; org: string };
  onNavigateSettings?: () => void;
}

const pageTitles: Record<NavigationPage, string> = {
  dashboard: 'Dashboard',
  scan: 'Scan Project',
  inventory: 'Crypto Inventory',
  certificates: 'Certificate & Cryptographic Artifacts',
  dependencies: 'Cryptographic Dependencies',
  quantum: 'Quantum Risk Analysis',
  pqc: 'PQC Migration Strategy',
  cbom: 'Cryptographic Bill of Materials',
  reports: 'Reports',
  settings: 'Settings',
};

export const TopBar: React.FC<TopBarProps> = ({
  activePage,
  backendStatus,
  searchQuery,
  onSearchChange,
  targetPath,
  theme = 'dark',
  onToggleTheme,
  user = { name: 'Security Analyst', email: 'analyst@ecdat.local', org: 'Enterprise SecOps' },
  onNavigateSettings,
}) => {
  const [showNotifications, setShowNotifications] = useState(false);

  return (
    <header className="topbar">
      <div className="topbar-left">
        <div className="breadcrumb-trail">
          <span>CRYPTAGUARD</span>
          <span className="breadcrumb-sep">/</span>
          <span className="breadcrumb-active">{pageTitles[activePage] || 'Overview'}</span>
          {targetPath && (
            <>
              <span className="breadcrumb-sep">/</span>
              <span className="font-mono text-muted" style={{ fontSize: '11px' }}>
                {targetPath.split(/[\\/]/).pop()}
              </span>
            </>
          )}
        </div>
      </div>

      <div className="topbar-right">
        {/* Global Search */}
        <div className="topbar-search-wrap">
          <svg className="topbar-search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <input
            type="text"
            className="topbar-search-input"
            placeholder="Search primitives, algos, files..."
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
          />
        </div>

        {/* Live Engine Status */}
        <div
          className={`engine-status-pill ${backendStatus.toLowerCase()}`}
          title={
            backendStatus === 'UP'
              ? 'Backend Analysis Service is online & ready.'
              : backendStatus === 'CHECKING'
              ? 'Checking backend connection...'
              : 'Backend service unreachable at configured endpoint.'
          }
        >
          <span className="status-indicator-dot"></span>
          <span>
            Analysis Engine: {backendStatus === 'UP' ? 'Online' : backendStatus === 'CHECKING' ? 'Checking' : 'Offline'}
          </span>
        </div>

        {/* Theme Toggle Button */}
        {onToggleTheme && (
          <button
            className="theme-toggle-btn"
            onClick={onToggleTheme}
            title={theme === 'dark' ? 'Switch to Light Theme' : 'Switch to Dark Theme'}
            aria-label={theme === 'dark' ? 'Switch to Light Theme' : 'Switch to Dark Theme'}
          >
            {theme === 'dark' ? (
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="12" cy="12" r="5" />
                <line x1="12" y1="1" x2="12" y2="3" />
                <line x1="12" y1="21" x2="12" y2="23" />
                <line x1="4.22" y1="4.22" x2="5.64" y2="5.64" />
                <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
                <line x1="1" y1="12" x2="3" y2="12" />
                <line x1="21" y1="12" x2="23" y2="12" />
                <line x1="4.22" y1="19.78" x2="5.64" y2="18.36" />
                <line x1="18.36" y1="5.64" x2="19.78" y2="4.22" />
              </svg>
            ) : (
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
              </svg>
            )}
          </button>
        )}

        {/* Notifications */}
        <div style={{ position: 'relative' }}>
          <button
            className="btn-secondary"
            style={{ padding: '6px 10px' }}
            onClick={() => setShowNotifications(!showNotifications)}
            title="System alerts"
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
              <path d="M13.73 21a2 2 0 0 1-3.46 0" />
            </svg>
          </button>
          {showNotifications && (
            <div
              style={{
                position: 'absolute',
                right: 0,
                top: '120%',
                width: 280,
                backgroundColor: 'var(--bg-surface-elevated)',
                border: '1px solid var(--border-default)',
                borderRadius: 'var(--radius-sm)',
                padding: '12px',
                boxShadow: 'var(--shadow-lg)',
                zIndex: 50,
                fontSize: '12px',
              }}
            >
              <div style={{ fontWeight: 600, marginBottom: '6px', color: 'var(--text-primary)' }}>
                System Status
              </div>
              <p style={{ color: 'var(--text-secondary)', lineHeight: 1.4 }}>
                NIST FIPS 203, 204, 205 rule sets active. Static AST scanner and archive extraction isolation ready.
              </p>
            </div>
          )}
        </div>

        {/* User Profile Pill (Reference Top Right) */}
        <div
          className="topbar-user-pill"
          onClick={onNavigateSettings}
          title={`${user.name} (${user.email})`}
          style={{ cursor: onNavigateSettings ? 'pointer' : 'default' }}
        >
          <div className="topbar-user-avatar">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </div>
          <span className="topbar-user-name">{user.name}</span>
        </div>
      </div>
    </header>
  );
};
