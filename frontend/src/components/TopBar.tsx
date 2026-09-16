import React, { useState } from 'react';
import type { NavigationPage } from './Sidebar';

interface TopBarProps {
  activePage: NavigationPage;
  backendStatus: 'UP' | 'DOWN' | 'CHECKING';
  searchQuery: string;
  onSearchChange: (query: string) => void;
  targetPath?: string;
}

const pageTitles: Record<NavigationPage, string> = {
  dashboard: 'Cryptographic Security Overview',
  scan: 'Scan Project Archive',
  inventory: 'Cryptographic Asset Inventory',
  certificates: 'Certificate & Cryptographic Artifacts',
  dependencies: 'Cryptographic Dependencies',
  quantum: 'Quantum Risk & Exposure Assessment',
  pqc: 'Post-Quantum Cryptography Migration',
  cbom: 'Cryptography Bill of Materials',
  reports: 'Security & Governance Reports',
  settings: 'Analysis & Platform Settings',
};

export const TopBar: React.FC<TopBarProps> = ({
  activePage,
  backendStatus,
  searchQuery,
  onSearchChange,
  targetPath,
}) => {
  const [showNotifications, setShowNotifications] = useState(false);

  return (
    <header className="topbar">
      <div className="topbar-left">
        <div className="breadcrumb-trail">
          <span>ECDAT</span>
          <span className="breadcrumb-sep">/</span>
          <span className="breadcrumb-active">{pageTitles[activePage]}</span>
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
      </div>
    </header>
  );
};
