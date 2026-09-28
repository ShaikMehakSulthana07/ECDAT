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
  assetCount,
  certCount,
  quantumCount,
}) => {
  return (
    <aside className={`sidebar ${isCollapsed ? 'collapsed' : ''}`}>
      {/* 1. Brand Header */}
      <div className="sidebar-header">
        <div className="sidebar-brand" onClick={() => onNavigate('dashboard')} style={{ cursor: 'pointer' }}>
          <div className="sidebar-brand-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              <path d="m9 12 2 2 4-4" />
            </svg>
          </div>
          {!isCollapsed && (
            <div className="sidebar-brand-text">
              <span className="sidebar-brand-title">CRYPTAGUARD</span>
              <span className="sidebar-brand-sub">Demo Workspace</span>
            </div>
          )}
        </div>

        <button
          className="sidebar-collapse-toggle"
          onClick={onToggleCollapse}
          title={isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'}
          aria-label="Toggle navigation collapse"
        >
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            {isCollapsed ? (
              <polyline points="9 18 15 12 9 6" />
            ) : (
              <polyline points="15 18 9 12 15 6" />
            )}
          </svg>
        </button>
      </div>

      {/* 2. New Scan Button */}
      {!isCollapsed && (
        <button
          className="sidebar-new-scan-btn"
          onClick={() => onNavigate('scan')}
          title="Start a new project scan"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          <span>New Scan</span>
        </button>
      )}

      {/* 3. User Profile Capsule (Matching Image 1 Sidebar layout) */}
      {!isCollapsed && (
        <div className="sidebar-user-capsule" onClick={() => onNavigate('settings')} title="View user profile & settings">
          <div className="sidebar-user-avatar">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </div>
          <div className="sidebar-user-info">
            <span className="sidebar-user-name">{user?.name || 'Security Analyst'}</span>
            <span className="sidebar-user-role">SecOps Specialist</span>
          </div>
          <div className="sidebar-user-chevron">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <polyline points="6 9 12 15 18 9" />
            </svg>
          </div>
        </div>
      )}

      {/* 4. Main Navigation List */}
      <nav className="sidebar-nav">
        <div className="nav-section-title">CORE ANALYSIS</div>

        <button
          className={`nav-item-btn ${activePage === 'dashboard' ? 'active' : ''}`}
          onClick={() => onNavigate('dashboard')}
          title="Dashboard Overview"
        >
          <span className="nav-item-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="3" width="7" height="7" rx="1.5" />
              <rect x="14" y="3" width="7" height="7" rx="1.5" />
              <rect x="14" y="14" width="7" height="7" rx="1.5" />
              <rect x="3" y="14" width="7" height="7" rx="1.5" />
            </svg>
          </span>
          {!isCollapsed && <span className="nav-item-label">Dashboard</span>}
        </button>

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
          {!isCollapsed && <span className="nav-item-label">Crypto Inventory</span>}
          {!isCollapsed && assetCount !== undefined && assetCount > 0 && (
            <span className="nav-item-badge">{assetCount}</span>
          )}
        </button>

        <button
          className={`nav-item-btn ${activePage === 'certificates' ? 'active' : ''}`}
          onClick={() => onNavigate('certificates')}
          title="Certificate & Cryptographic Artifacts"
        >
          <span className="nav-item-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <rect x="3" y="4" width="18" height="16" rx="2" />
              <circle cx="9" cy="10" r="2" />
              <line x1="15" y1="8" x2="17" y2="8" />
              <line x1="15" y1="12" x2="17" y2="12" />
              <line x1="7" y1="16" x2="17" y2="16" />
            </svg>
          </span>
          {!isCollapsed && <span className="nav-item-label">Certificates</span>}
          {!isCollapsed && certCount !== undefined && certCount > 0 && (
            <span className="nav-item-badge">{certCount}</span>
          )}
        </button>

        <button
          className={`nav-item-btn ${activePage === 'dependencies' ? 'active' : ''}`}
          onClick={() => onNavigate('dependencies')}
          title="Cryptographic Dependencies & Libraries"
        >
          <span className="nav-item-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M16.5 9.4 7.55 4.24a1.78 1.78 0 0 0-2.5 1.55v12.42a1.78 1.78 0 0 0 2.5 1.55L16.5 14.6a1.78 1.78 0 0 0 0-3.2z" />
              <path d="m21 16-4-2.5v-3L21 8" />
            </svg>
          </span>
          {!isCollapsed && <span className="nav-item-label">Dependencies</span>}
        </button>

        <div className="nav-section-title" style={{ marginTop: '12px' }}>QUANTUM POSTURE</div>

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
          {!isCollapsed && <span className="nav-item-label">Quantum Risk</span>}
          {!isCollapsed && quantumCount !== undefined && quantumCount > 0 && (
            <span className="nav-item-badge alert">{quantumCount}</span>
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
      </nav>

      {/* 4. Bottom Nav Items: Settings & Logout */}
      <div className="sidebar-bottom-nav">
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

        <button
          className="nav-item-btn btn-logout-item"
          onClick={onLogout}
          title="Sign Out"
        >
          <span className="nav-item-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
              <polyline points="16 17 21 12 16 7" />
              <line x1="21" y1="12" x2="9" y2="12" />
            </svg>
          </span>
          {!isCollapsed && <span className="nav-item-label">Logout</span>}
        </button>
      </div>
    </aside>
  );
};
