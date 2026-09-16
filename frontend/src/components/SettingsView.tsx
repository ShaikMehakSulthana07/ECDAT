import React from 'react';
import { apiService } from '../services/api';

interface SettingsViewProps {
  user: { name: string; email: string; org: string };
  backendStatus: 'UP' | 'DOWN' | 'CHECKING';
  onHealthCheck: () => void;
}

export const SettingsView: React.FC<SettingsViewProps> = ({
  user,
  backendStatus,
  onHealthCheck,
}) => {
  return (
    <div>
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Platform &amp; Analysis Settings</h1>
          <p className="view-subtitle">
            Configuration parameters for cryptographic discovery engines, default Mosca quantum parameters, and platform integration.
          </p>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '20px', maxWidth: '840px' }}>
        {/* Profile Card */}
        <div className="card-panel">
          <h3 className="card-panel-title" style={{ marginBottom: '16px' }}>
            Application Session Profile
          </h3>
          <table className="props-table">
            <tbody>
              <tr>
                <th>Session Operator</th>
                <td className="font-bold">{user.name}</td>
              </tr>
              <tr>
                <th>Contact Email</th>
                <td>{user.email}</td>
              </tr>
              <tr>
                <th>Organization</th>
                <td>{user.org}</td>
              </tr>
              <tr>
                <th>Session Environment</th>
                <td>
                  <span className="tag-subtle font-mono">Local Analysis Session</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

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
      </div>
    </div>
  );
};
