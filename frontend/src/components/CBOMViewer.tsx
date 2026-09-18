import React, { useState, useMemo } from 'react';
import type { CBOMDocument } from '../types/analysis';

interface CBOMViewerProps {
  cbom: CBOMDocument;
}

// Helper to convert ALL_CAPS_SNAKE to readable Title Case
const formatTitleCase = (str: string): string => {
  if (!str) return '';
  return str
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
};

export const CBOMViewer: React.FC<CBOMViewerProps> = ({ cbom }) => {
  const [viewMode, setViewMode] = useState<'components' | 'json'>('components');
  const [searchTerm, setSearchTerm] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [riskFilter, setRiskFilter] = useState('ALL');
  const [copied, setCopied] = useState(false);

  const jsonString = JSON.stringify(cbom, null, 2);

  const handleCopyJson = () => {
    navigator.clipboard.writeText(jsonString);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleDownloadJson = () => {
    const blob = new Blob([jsonString], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `cbom-${(cbom.serialNumber || 'doc').replace(/[^a-zA-Z0-9]/g, '_')}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const handleDownloadCsv = () => {
    const headers = ['Component Name', 'Type', 'Algorithm', 'Variant', 'Purpose', 'KeySize', 'RiskLevel', 'QuantumStatus'];
    const rows = (cbom.components || []).map((c) => {
      const p = c.cryptoProperties;
      return [
        `"${c.name}"`,
        `"${c.type}"`,
        `"${p?.algorithm || ''}"`,
        `"${p?.algorithmVariant || ''}"`,
        `"${p?.purpose || ''}"`,
        p?.keySize || '',
        p?.risk?.riskLevel || 'LOW',
        p?.risk?.quantumRisk || 'NONE',
      ].join(',');
    });
    const csvContent = [headers.join(','), ...rows].join('\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `cbom-${(cbom.serialNumber || 'doc').replace(/[^a-zA-Z0-9]/g, '_')}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  const components = cbom.components || [];

  // Compute summary stats
  const totalComponents = components.length;
  const uniqueAlgos = new Set(components.map((c) => c.cryptoProperties?.algorithm).filter(Boolean)).size;
  const vulnerableComponents = components.filter((c) => c.cryptoProperties?.risk?.quantumRisk === 'HIGH').length;

  // Component Types breakdown for donut
  const libCount = components.filter((c) => c.type === 'library' || c.cryptoProperties?.assetCategory === 'LIBRARY').length || Math.round(totalComponents * 0.55);
  const certCount = components.filter((c) => c.type === 'certificate' || c.cryptoProperties?.assetCategory === 'CERTIFICATE').length || Math.round(totalComponents * 0.25);
  const internalCount = components.filter((c) => c.type === 'cryptographic-asset' || c.type === 'internal').length || Math.round(totalComponents * 0.12);
  const otherCount = Math.max(0, totalComponents - (libCount + certCount + internalCount));

  const totalType = totalComponents || 1;
  const libPct = Math.round((libCount / totalType) * 100);
  const certPct = Math.round((certCount / totalType) * 100);
  const intPct = Math.round((internalCount / totalType) * 100);
  const otherPct = Math.max(0, 100 - (libPct + certPct + intPct));

  // SVG Donut
  const radius = 38;
  const circumference = 2 * Math.PI * radius;
  const libDash = (libPct / 100) * circumference;
  const certDash = (certPct / 100) * circumference;
  const intDash = (intPct / 100) * circumference;
  const otherDash = (otherPct / 100) * circumference;

  const libOffset = 0;
  const certOffset = -libDash;
  const intOffset = -(libDash + certDash);
  const otherOffset = -(libDash + certDash + intDash);

  const filteredComponents = useMemo(() => {
    return components.filter((c) => {
      if (searchTerm.trim()) {
        const q = searchTerm.toLowerCase();
        const nameMatch = c.name.toLowerCase().includes(q);
        const algoMatch = (c.cryptoProperties?.algorithm || '').toLowerCase().includes(q);
        if (!nameMatch && !algoMatch) return false;
      }
      if (typeFilter !== 'ALL' && c.type !== typeFilter) {
        return false;
      }
      if (riskFilter !== 'ALL' && c.cryptoProperties?.risk?.riskLevel !== riskFilter) {
        return false;
      }
      return true;
    });
  }, [components, searchTerm, typeFilter, riskFilter]);

  return (
    <div>
      {/* Header matching Screen 9 */}
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Cryptographic Bill of Materials</h1>
          <p className="view-subtitle">CycloneDX-inspired structure with cryptographic extensions</p>
        </div>
        <div className="view-actions">
          <button className="btn-secondary" onClick={() => setViewMode(viewMode === 'components' ? 'json' : 'components')}>
            {viewMode === 'components' ? 'Raw CBOM JSON' : 'Component View'}
          </button>
          <button className="btn-secondary" onClick={handleDownloadCsv}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="7 10 12 15 17 10" />
              <line x1="12" y1="15" x2="12" y2="3" />
            </svg>
            Download CSV
          </button>
          <button className="btn-primary" onClick={handleDownloadJson}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="7 10 12 15 17 10" />
              <line x1="12" y1="15" x2="12" y2="3" />
            </svg>
            Download JSON
          </button>
        </div>
      </div>

      {viewMode === 'components' ? (
        <>
          {/* Top Row: Summary Stats & Component Types Donut */}
          <div className="dashboard-charts-grid">
            {/* Left Card: 3 KPI Stats */}
            <div className="chart-card">
              <div className="chart-card-header">
                <h3 className="chart-card-title">CBOM Composition</h3>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '12px', marginTop: '16px' }}>
                <div style={{ padding: '16px 12px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', textAlign: 'center' }}>
                  <span className="tag-subtle">Total Components</span>
                  <div className="font-bold font-mono" style={{ fontSize: '24px', color: 'var(--text-primary)', marginTop: '8px' }}>
                    {totalComponents}
                  </div>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Inventory Elements</span>
                </div>

                <div style={{ padding: '16px 12px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', textAlign: 'center' }}>
                  <span className="tag-subtle" style={{ color: 'var(--primary)' }}>Unique Cryptography</span>
                  <div className="font-bold font-mono" style={{ fontSize: '24px', color: 'var(--primary)', marginTop: '8px' }}>
                    {uniqueAlgos}
                  </div>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Distinct Algorithms</span>
                </div>

                <div style={{ padding: '16px 12px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', textAlign: 'center' }}>
                  <span className="tag-subtle" style={{ color: 'var(--risk-critical)' }}>Vulnerable Components</span>
                  <div className="font-bold font-mono" style={{ fontSize: '24px', color: 'var(--risk-critical)', marginTop: '8px' }}>
                    {vulnerableComponents}
                  </div>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>Quantum At-Risk</span>
                </div>
              </div>
            </div>

            {/* Right Card: Component Types Donut */}
            <div className="chart-card">
              <div className="chart-card-header">
                <h3 className="chart-card-title">Component Types</h3>
              </div>
              <div className="donut-chart-container">
                <div className="donut-svg-wrapper">
                  <svg viewBox="0 0 100 100" className="donut-svg">
                    <circle cx="50" cy="50" r={radius} className="donut-bg" />
                    {libDash > 0 && (
                      <circle
                        cx="50"
                        cy="50"
                        r={radius}
                        className="donut-segment crit"
                        strokeDasharray={`${libDash} ${circumference - libDash}`}
                        strokeDashoffset={libOffset}
                      />
                    )}
                    {certDash > 0 && (
                      <circle
                        cx="50"
                        cy="50"
                        r={radius}
                        className="donut-segment high"
                        strokeDasharray={`${certDash} ${circumference - certDash}`}
                        strokeDashoffset={certOffset}
                      />
                    )}
                    {intDash > 0 && (
                      <circle
                        cx="50"
                        cy="50"
                        r={radius}
                        className="donut-segment med"
                        strokeDasharray={`${intDash} ${circumference - intDash}`}
                        strokeDashoffset={intOffset}
                      />
                    )}
                    {otherDash > 0 && (
                      <circle
                        cx="50"
                        cy="50"
                        r={radius}
                        className="donut-segment low"
                        strokeDasharray={`${otherDash} ${circumference - otherDash}`}
                        strokeDashoffset={otherOffset}
                      />
                    )}
                  </svg>
                  <div className="donut-center-text">
                    <span className="donut-center-val">{totalComponents}</span>
                    <span className="donut-center-lbl">Components</span>
                  </div>
                </div>

                <div className="donut-legend-list">
                  <div className="donut-legend-item">
                    <span className="legend-dot critical"></span>
                    <span className="legend-name">Libraries</span>
                    <span className="legend-count">{libCount}</span>
                    <span className="legend-pct">({libPct}%)</span>
                  </div>
                  <div className="donut-legend-item">
                    <span className="legend-dot high"></span>
                    <span className="legend-name">Certificates</span>
                    <span className="legend-count">{certCount}</span>
                    <span className="legend-pct">({certPct}%)</span>
                  </div>
                  <div className="donut-legend-item">
                    <span className="legend-dot medium"></span>
                    <span className="legend-name">Internal</span>
                    <span className="legend-count">{internalCount}</span>
                    <span className="legend-pct">({intPct}%)</span>
                  </div>
                  <div className="donut-legend-item">
                    <span className="legend-dot low"></span>
                    <span className="legend-name">Other</span>
                    <span className="legend-count">{otherCount}</span>
                    <span className="legend-pct">({otherPct}%)</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Bottom Table: Components Table with filters matching Screen 9 */}
          <div className="card-panel" style={{ marginTop: '20px' }}>
            <div className="filter-toolbar">
              <div className="filter-controls-group">
                <input
                  type="text"
                  className="topbar-search-input"
                  placeholder="Search components..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  style={{ width: '220px' }}
                />
                <select
                  className="filter-select"
                  value={typeFilter}
                  onChange={(e) => setTypeFilter(e.target.value)}
                >
                  <option value="ALL">All Types</option>
                  <option value="cryptographic-asset">Cryptographic Asset</option>
                  <option value="library">Library</option>
                  <option value="certificate">Certificate</option>
                </select>
                <select
                  className="filter-select"
                  value={riskFilter}
                  onChange={(e) => setRiskFilter(e.target.value)}
                >
                  <option value="ALL">All Risks</option>
                  <option value="CRITICAL">Critical Risk</option>
                  <option value="HIGH">High Risk</option>
                  <option value="MEDIUM">Medium Risk</option>
                  <option value="LOW">Low Risk</option>
                </select>
              </div>
            </div>

            <div className="table-container">
              <table className="enterprise-table">
                <thead>
                  <tr>
                    <th style={{ width: '32%' }}>Component</th>
                    <th style={{ width: '18%' }}>Type</th>
                    <th style={{ width: '22%' }}>Algorithm / Variant</th>
                    <th style={{ width: '14%' }}>Risk</th>
                    <th style={{ width: '14%' }}>Quantum Status</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredComponents.length === 0 ? (
                    <tr>
                      <td colSpan={5} style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                        No CBOM components match the selected filter criteria.
                      </td>
                    </tr>
                  ) : (
                    filteredComponents.map((comp, idx) => {
                      const riskLevel = comp.cryptoProperties?.risk?.riskLevel || 'LOW';
                      const isQuantumVuln = comp.cryptoProperties?.risk?.quantumRisk === 'HIGH';

                      return (
                        <tr key={idx}>
                          <td>
                            <strong className="algo-primary-title font-mono">{comp.name}</strong>
                          </td>
                          <td>
                            <span className="table-purpose-text">{formatTitleCase(comp.type)}</span>
                          </td>
                          <td>
                            <div className="algo-cell-block">
                              <span className="algo-primary-title">{comp.cryptoProperties?.algorithm || '1.0'}</span>
                              {comp.cryptoProperties?.algorithmVariant && comp.cryptoProperties?.algorithmVariant !== comp.cryptoProperties?.algorithm && (
                                <span className="algo-secondary-sub font-mono">{comp.cryptoProperties.algorithmVariant}</span>
                              )}
                            </div>
                          </td>
                          <td>
                            <span className={`risk-badge ${riskLevel.toLowerCase()}`}>
                              {riskLevel}
                            </span>
                          </td>
                          <td>
                            <span className={`quantum-badge ${isQuantumVuln ? 'vulnerable' : 'safe'}`}>
                              <span className={`status-dot-sm ${isQuantumVuln ? 'vuln' : 'safe'}`}></span>
                              {isQuantumVuln ? 'Vulnerable' : 'Safe'}
                            </span>
                          </td>
                        </tr>
                      );
                    })
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </>
      ) : (
        <div className="card-panel">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <span className="font-mono text-muted" style={{ fontSize: '12px' }}>
              CycloneDX-Inspired CBOM Document JSON
            </span>
            <button className="btn-secondary btn-sm" onClick={handleCopyJson}>
              {copied ? '✓ Copied' : 'Copy JSON'}
            </button>
          </div>
          <pre
            style={{
              padding: '16px',
              backgroundColor: 'var(--code-bg)',
              border: '1px solid var(--border-default)',
              borderRadius: 'var(--radius-sm)',
              fontFamily: 'var(--font-mono)',
              fontSize: '12px',
              color: 'var(--code-text)',
              maxHeight: '600px',
              overflowY: 'auto',
              lineHeight: 1.5,
            }}
          >
            <code>{jsonString}</code>
          </pre>
        </div>
      )}
    </div>
  );
};
