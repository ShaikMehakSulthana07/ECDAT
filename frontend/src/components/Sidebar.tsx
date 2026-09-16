import React from 'react';

export type NavigationPage =
  | 'dashboard'
  | 'scan'
  | 'inventory'
  | 'certificates'
  | 'dependencies'
  | 'quantum'
  | 'pqc'
  | 'cbom'
  | 'reports'
  | 'settings';

interface SidebarProps {
  activePage: NavigationPage;
  onNavigate: (page: NavigationPage) => void;
  isCollapsed: boolean;
  onToggleCollapse: () => void;
  user: { name: string; email: string; org: string };
  onLogout: () => void;
  assetCount?: number;
  certCount?: number;
  quantumCount?: number;
}

export const Sidebar: React.FC<SidebarProps> = ({
  activePage,
  onNavigate,
  isCollapsed,
  onToggleCollapse,
  user,
  onLogout,
  assetCount = 0,
  certCount = 0,
  quantumCount = 0,
}) => {
  return (
    <aside className={`sidebar ${isCollapsed ? 'collapsed' : ''}`}>
      {/* Brand Header */}
      <div className="sidebar-header">
        <div className="sidebar-brand">
          <div className="sidebar-brand-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              <path d="m9 12 2 2 4-4" />
            </svg>
          </div>
          {!isCollapsed && (
            <div className="sidebar-brand-text">
              <span className="sidebar-brand-title">ECDAT</span>
              <span className="sidebar-brand-sub">Cryptographic Security</span>
            </div>
          )}
        </div>

        <button
          className="sidebar-collapse-toggle"
          onClick={onToggleCollapse}
          title={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          aria-label="Toggle navigation collapse"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            {isCollapsed ? (
              <polyline points="9 18 15 12 9 6" />
            ) : (
              <polyline points="15 18 9 12 15 6" />
            )}
          </svg>
        </button>
      </div>

      {/* Navigation Sections */}
      <nav className="sidebar-nav">
        {/* OVERVIEW */}
        <div className="nav-section">
          {!isCollapsed && <div className="nav-section-title">Overview</div>}
          <button
            className={`nav-item-btn ${activePage === 'dashboard' ? 'active' : ''}`}
            onClick={() => onNavigate('dashboard')}
            title="Dashboard Overview"
          >
            <span className="nav-item-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="3" y="3" width="7" height="7" />
                <rect x="14" y="3" width="7" height="7" />
                <rect x="14" y="14" width="7" height="7" />
                <rect x="3" y="14" width="7" height="7" />
              </svg>
            </span>
            {!isCollapsed && <span className="nav-item-label">Dashboard</span>}
          </button>
        </div>

        {/* DISCOVERY */}
        <div className="nav-section">
          {!isCollapsed && <div className="nav-section-title">Discovery</div>}
          <button
            className={`nav-item-btn ${activePage === 'scan' ? 'active' : ''}`}
            onClick={() => onNavigate('scan')}
            title="Scan Project Archive"
          >
            <span className="nav-item-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="17 8 12 3 7 8" />
                <line x1="12" y1="3" x2="12" y2="9" />
              </svg>
            </span>
            {!isCollapsed && <span className="nav-item-label">Scan Project</span>}
          </button>

          <button
            className={`nav-item-btn ${activePage === 'inventory' ? 'active' : ''}`}
            onClick={() => onNavigate('inventory')}
            title="Crypto Inventory"
          >
            <span className="nav-item-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="2" y="2" width="20" height="8" rx="2" ry="2" />
                <rect x="2" y="14" width="20" height="8" rx="2" ry="2" />
                <line x1="6" y1="6" x2="6.01" y2="6" />
                <line x1="6" y1="18" x2="6.01" y2="18" />
              </svg>
            </span>
            {!isCollapsed && (
              <>
                <span className="nav-item-label">Crypto Inventory</span>
                {assetCount > 0 && <span className="nav-item-badge">{assetCount}</span>}
              </>
            )}
          </button>

          <button
            className={`nav-item-btn ${activePage === 'certificates' ? 'active' : ''}`}
            onClick={() => onNavigate('certificates')}
            title="Certificates & Artifacts"
          >
            <span className="nav-item-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="8" r="6" />
                <path d="M15.477 12.89 17 22l-5-3-5 3 1.523-9.11" />
              </svg>
            </span>
            {!isCollapsed && (
              <>
                <span className="nav-item-label">Certificates</span>
                {certCount > 0 && <span className="nav-item-badge">{certCount}</span>}
              </>
            )}
          </button>

          <button
            className={`nav-item-btn ${activePage === 'dependencies' ? 'active' : ''}`}
            onClick={() => onNavigate('dependencies')}
            title="Cryptographic Dependencies"
          >
            <span className="nav-item-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="m7.5 4.27 9 5.15" />
                <path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z" />
                <path d="m3.3 7 8.7 5 8.7-5" />
                <path d="M12 22V12" />
              </svg>
            </span>
            {!isCollapsed && <span className="nav-item-label">Dependencies</span>}
          </button>
        </div>

        {/* RISK & MIGRATION */}
        <div className="nav-section">
          {!isCollapsed && <div className="nav-section-title">Risk &amp; Migration</div>}
          <button
            className={`nav-item-btn ${activePage === 'quantum' ? 'active' : ''}`}
            onClick={() => onNavigate('quantum')}
            title="Quantum Risk Exposure"
          >
            <span className="nav-item-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="2" y1="12" x2="22" y2="12" />
                <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
              </svg>
            </span>
            {!isCollapsed && (
              <>
                <span className="nav-item-label">Quantum Risk</span>
                {quantumCount > 0 && (
                  <span className="nav-item-badge" style={{ color: 'var(--risk-high)', borderColor: 'var(--risk-high-border)' }}>
                    {quantumCount}
                  </span>
                )}
              </>
            )}
          </button>

          <button
            className={`nav-item-btn ${activePage === 'pqc' ? 'active' : ''}`}
            onClick={() => onNavigate('pqc')}
            title="Post-Quantum Cryptography Migration"
          >
            <span className="nav-item-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="16 3 21 3 21 8" />
                <line x1="4" y1="20" x2="21" y2="3" />
                <polyline points="21 16 21 21 16 21" />
                <line x1="15" y1="15" x2="21" y2="21" />
                <line x1="4" y1="4" x2="9" y2="9" />
              </svg>
            </span>
            {!isCollapsed && <span className="nav-item-label">PQC Migration</span>}
          </button>
        </div>

        {/* GOVERNANCE */}
        <div className="nav-section">
          {!isCollapsed && <div className="nav-section-title">Governance</div>}
          <button
            className={`nav-item-btn ${activePage === 'cbom' ? 'active' : ''}`}
            onClick={() => onNavigate('cbom')}
            title="Cryptography Bill of Materials"
          >
            <span className="nav-item-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
                <polyline points="10 9 9 9 8 9" />
              </svg>
            </span>
            {!isCollapsed && <span className="nav-item-label">CBOM</span>}
          </button>

          <button
            className={`nav-item-btn ${activePage === 'reports' ? 'active' : ''}`}
            onClick={() => onNavigate('reports')}
            title="Security & Compliance Reports"
          >
            <span className="nav-item-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="12" y1="18" x2="12" y2="12" />
                <line x1="9" y1="15" x2="15" y2="15" />
              </svg>
            </span>
            {!isCollapsed && <span className="nav-item-label">Reports</span>}
          </button>
        </div>

        {/* SYSTEM */}
        <div className="nav-section">
          {!isCollapsed && <div className="nav-section-title">System</div>}
          <button
            className={`nav-item-btn ${activePage === 'settings' ? 'active' : ''}`}
            onClick={() => onNavigate('settings')}
            title="Analysis Configuration & Settings"
          >
            <span className="nav-item-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="3" />
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
              </svg>
            </span>
            {!isCollapsed && <span className="nav-item-label">Settings</span>}
          </button>
        </div>
      </nav>

      {/* Footer Profile & Logout */}
      <div className="sidebar-footer">
        <div className="sidebar-user-block">
          <div className="user-info">
            <div className="user-avatar" title={user.name}>
              {user.name.split(' ').map((n) => n[0]).join('').slice(0, 2)}
            </div>
            {!isCollapsed && (
              <div className="user-details">
                <span className="user-name">{user.name}</span>
                <span className="user-org">{user.org}</span>
              </div>
            )}
          </div>
          {!isCollapsed && (
            <button className="btn-logout" onClick={onLogout} title="Sign Out">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
                <polyline points="16 17 21 12 16 7" />
                <line x1="21" y1="12" x2="9" y2="12" />
              </svg>
            </button>
          )}
        </div>
      </div>
    </aside>
  );
};
