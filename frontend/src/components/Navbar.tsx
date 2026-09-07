import React from 'react';

interface NavbarProps {
  backendStatus: 'UP' | 'DOWN' | 'CHECKING';
  activeTab: 'overview' | 'findings' | 'pqc' | 'cbom' | 'traceability';
  setActiveTab: (tab: 'overview' | 'findings' | 'pqc' | 'cbom' | 'traceability') => void;
  hasResults: boolean;
  totalFindings?: number;
  onNewScan?: () => void;
}

export const Navbar: React.FC<NavbarProps> = ({
  backendStatus,
  activeTab,
  setActiveTab,
  hasResults,
  totalFindings = 0,
  onNewScan,
}) => {
  return (
    <header className="navbar">
      <div className="navbar-container">
        <div className="navbar-brand">
          <div className="brand-logo">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              <path d="M9 12l2 2 4-4" />
            </svg>
          </div>
          <div className="brand-text">
            <div className="brand-title-row">
              <span className="brand-title">ECDAT</span>
              <span className="badge-sih">SIH26164</span>
            </div>
            <span className="brand-subtitle">Enterprise Cryptographic Discovery & Analysis Tool</span>
          </div>
        </div>

        {hasResults && (
          <nav className="navbar-nav">
            <button
              className={`nav-link ${activeTab === 'overview' ? 'active' : ''}`}
              onClick={() => setActiveTab('overview')}
            >
              Overview
            </button>
            <button
              className={`nav-link ${activeTab === 'findings' ? 'active' : ''}`}
              onClick={() => setActiveTab('findings')}
            >
              Findings
              <span className="nav-badge">{totalFindings}</span>
            </button>
            <button
              className={`nav-link ${activeTab === 'pqc' ? 'active' : ''}`}
              onClick={() => setActiveTab('pqc')}
            >
              PQC Migration
            </button>
            <button
              className={`nav-link ${activeTab === 'cbom' ? 'active' : ''}`}
              onClick={() => setActiveTab('cbom')}
            >
              CycloneDX CBOM
            </button>
            <button
              className={`nav-link ${activeTab === 'traceability' ? 'active' : ''}`}
              onClick={() => setActiveTab('traceability')}
            >
              Traceability
            </button>
          </nav>
        )}

        <div className="navbar-actions">
          {hasResults && onNewScan && (
            <button className="btn-secondary btn-sm" onClick={onNewScan}>
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
                <path d="M18 2v4h4" />
                <path d="M22 2l-6 6" />
              </svg>
              New Scan
            </button>
          )}

          <div className={`status-pill ${backendStatus.toLowerCase()}`} title={`Backend Status: ${backendStatus}`}>
            <span className="status-dot"></span>
            <span className="status-label">API {backendStatus === 'UP' ? 'Connected' : backendStatus === 'CHECKING' ? 'Connecting...' : 'Offline'}</span>
          </div>
        </div>
      </div>
    </header>
  );
};
