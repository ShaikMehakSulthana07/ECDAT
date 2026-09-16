import React from 'react';
import type { AnalysisResponse } from '../types/analysis';

interface DashboardViewProps {
  analysisData: AnalysisResponse | null;
  onNavigate: (page: 'scan' | 'inventory' | 'quantum' | 'pqc' | 'cbom') => void;
  onSelectFinding: (index: number) => void;
  onQuickScan: () => void;
  isLoading: boolean;
}

export const DashboardView: React.FC<DashboardViewProps> = ({
  analysisData,
  onNavigate,
  onSelectFinding,
  onQuickScan,
  isLoading,
}) => {
  if (!analysisData) {
    return (
      <div className="empty-state-card">
        <div className="empty-state-icon">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <rect x="2" y="2" width="20" height="8" rx="2" ry="2" />
            <rect x="2" y="14" width="20" height="8" rx="2" ry="2" />
            <line x1="6" y1="6" x2="6.01" y2="6" />
            <line x1="6" y1="18" x2="6.01" y2="18" />
          </svg>
        </div>
        <h2 className="empty-state-title">No Analysis Available</h2>
        <p className="empty-state-desc">
          Upload a project archive (.zip) or select a project directory to discover cryptographic primitives, evaluate quantum risk exposure, and generate post-quantum migration plans.
        </p>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn-primary" onClick={() => onNavigate('scan')}>
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="17 8 12 3 7 8" />
              <line x1="12" y1="3" x2="12" y2="9" />
            </svg>
            Scan Project Archive
          </button>
          <button className="btn-secondary" onClick={onQuickScan} disabled={isLoading}>
            {isLoading ? 'Scanning...' : 'Scan Default Target'}
          </button>
        </div>
      </div>
    );
  }

  const { summary, findings, riskAssessments, pqcRecommendations, context } = analysisData;

  // Calculate high/critical count
  const highCriticalCount = (summary.highRiskCount || 0) + (summary.criticalRiskCount || 0);

  // Derive quantum exposure metrics from findings
  const quantumVulnerableCount = summary.quantumHighRiskCount || 0;
  const quantumSafeCount = Math.max(0, summary.totalFindings - quantumVulnerableCount);

  // Mosca calculation parameters from context or default
  const migrationTime = context?.migrationTimeYears ?? 3;
  const dataLifetime = context?.dataLifetimeYears ?? 10;
  const threatHorizon = context?.threatHorizonYears ?? 10;
  const totalExposure = migrationTime + dataLifetime;
  const moscaConditionMet = totalExposure > threatHorizon;

  // Risk bar percentages
  const total = summary.totalFindings || 1;
  const critPct = Math.round(((summary.criticalRiskCount || 0) / total) * 100);
  const highPct = Math.round(((summary.highRiskCount || 0) / total) * 100);
  const medPct = Math.round(((summary.mediumRiskCount || 0) / total) * 100);
  const lowPct = Math.round(((summary.lowRiskCount || 0) / total) * 100);

  return (
    <div>
      {/* Header */}
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Cryptographic Security Overview</h1>
          <p className="view-subtitle">
            Visibility into cryptographic assets, quantum exposure, and migration readiness.
          </p>
        </div>
        <div className="view-actions">
          <button className="btn-secondary" onClick={() => onNavigate('scan')}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="17 8 12 3 7 8" />
              <line x1="12" y1="3" x2="12" y2="9" />
            </svg>
            New Scan
          </button>
          <button className="btn-primary" onClick={() => onNavigate('cbom')}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
            </svg>
            View CBOM
          </button>
        </div>
      </div>

      {/* Top Metrics Cards (5 Cards Driven by Actual API Data) */}
      <div className="metrics-grid">
        <div className="metric-card" onClick={() => onNavigate('inventory')} style={{ cursor: 'pointer' }}>
          <div className="metric-card-top">
            <span className="metric-card-label">Total Crypto Assets</span>
            <span className="metric-card-icon">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="2" y="2" width="20" height="8" rx="2" />
                <rect x="2" y="14" width="20" height="8" rx="2" />
              </svg>
            </span>
          </div>
          <div className="metric-card-val">{summary.totalFindings}</div>
          <div className="metric-card-footer">Discovered cryptographic primitives</div>
        </div>

        <div className="metric-card quantum" onClick={() => onNavigate('quantum')} style={{ cursor: 'pointer' }}>
          <div className="metric-card-top">
            <span className="metric-card-label">Quantum Vulnerable</span>
            <span className="metric-card-icon" style={{ color: 'var(--risk-high)' }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="2" y1="12" x2="22" y2="12" />
              </svg>
            </span>
          </div>
          <div className="metric-card-val">{summary.quantumHighRiskCount || 0}</div>
          <div className="metric-card-footer">Vulnerable to Shor's algorithm</div>
        </div>

        <div className="metric-card critical" onClick={() => onNavigate('inventory')} style={{ cursor: 'pointer' }}>
          <div className="metric-card-top">
            <span className="metric-card-label">High / Critical Risk</span>
            <span className="metric-card-icon" style={{ color: 'var(--risk-critical)' }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
              </svg>
            </span>
          </div>
          <div className="metric-card-val">{highCriticalCount}</div>
          <div className="metric-card-footer">{summary.criticalRiskCount || 0} Critical · {summary.highRiskCount || 0} High</div>
        </div>

        <div className="metric-card pqc" onClick={() => onNavigate('pqc')} style={{ cursor: 'pointer' }}>
          <div className="metric-card-top">
            <span className="metric-card-label">Migration Required</span>
            <span className="metric-card-icon" style={{ color: 'var(--pqc-accent)' }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="16 3 21 3 21 8" />
                <line x1="4" y1="20" x2="21" y2="3" />
              </svg>
            </span>
          </div>
          <div className="metric-card-val">{summary.pqcRecommendedCount || 0}</div>
          <div className="metric-card-footer">Immediate PQC transition needed</div>
        </div>

        <div className="metric-card" onClick={() => onNavigate('pqc')} style={{ cursor: 'pointer' }}>
          <div className="metric-card-top">
            <span className="metric-card-label">PQC Recommendations</span>
            <span className="metric-card-icon">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
              </svg>
            </span>
          </div>
          <div className="metric-card-val">{pqcRecommendations.length}</div>
          <div className="metric-card-footer">Aligned with NIST FIPS 203/204/205</div>
        </div>
      </div>

      {/* Prominent Mosca Quantum Exposure Assessment Panel */}
      <div className="mosca-panel">
        <div className="mosca-panel-header">
          <div>
            <h3 className="card-panel-title">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
              </svg>
              Quantum Exposure Assessment (Mosca's Theorem)
            </h3>
            <span className="card-panel-sub">
              Formula: Migration Time (X) + Data Lifetime (Y) &gt; Threat Horizon (Z)
            </span>
          </div>
          <div className="mosca-verdict-box">
            <span className={`mosca-verdict-tag ${moscaConditionMet ? 'required' : 'safe'}`}>
              {moscaConditionMet ? 'MIGRATION REQUIRED' : 'MIGRATION NOT REQUIRED'}
            </span>
          </div>
        </div>

        <div className="mosca-formula-container">
          <div className="mosca-equation-block">
            <div className="mosca-term">
              <span className="term-label">Migration Time (X)</span>
              <span className="term-val">{migrationTime} yrs</span>
            </div>
            <span className="equation-operator">+</span>
            <div className="mosca-term">
              <span className="term-label">Data Lifetime (Y)</span>
              <span className="term-val">{dataLifetime} yrs</span>
            </div>
            <span className="equation-operator">=</span>
            <div className="mosca-term">
              <span className="term-label">Total Exposure (X+Y)</span>
              <span className="term-val font-mono" style={{ color: moscaConditionMet ? 'var(--risk-critical)' : 'var(--risk-low)' }}>
                {totalExposure} yrs
              </span>
            </div>
            <span className="equation-operator">&gt;</span>
            <div className="mosca-term">
              <span className="term-label">Threat Horizon (Z)</span>
              <span className="term-val font-mono">{threatHorizon} yrs</span>
            </div>
          </div>

          <div style={{ textAlign: 'right', fontSize: '12px', color: 'var(--text-secondary)' }}>
            <div>Condition: <strong>{totalExposure} &gt; {threatHorizon} = {moscaConditionMet ? 'TRUE' : 'FALSE'}</strong></div>
            <div style={{ marginTop: '4px', color: 'var(--text-muted)' }}>
              Application: <code>{context?.applicationName || 'Default'}</code>
            </div>
          </div>
        </div>

        <p className="mosca-explanation-text">
          {moscaConditionMet
            ? `Because data must remain confidential for ${dataLifetime} years and system migration will require ${migrationTime} years (${totalExposure} years total exposure), post-quantum migration must commence immediately before the quantum threat horizon (${threatHorizon} years).`
            : `Total exposure (${totalExposure} years) does not exceed the estimated quantum threat horizon (${threatHorizon} years). Regular surveillance recommended.`}
        </p>
      </div>

      {/* Visualizations Row */}
      <div className="visualizations-grid">
        {/* 1. Risk Distribution */}
        <div className="vis-card">
          <div className="vis-header">
            <span>Risk Distribution</span>
            <span className="tag-subtle">0–100 Scale</span>
          </div>
          <div className="stacked-bar-wrapper">
            <div className="stacked-bar">
              {critPct > 0 && <div className="bar-segment critical" style={{ width: `${critPct}%` }} title={`Critical: ${summary.criticalRiskCount}`} />}
              {highPct > 0 && <div className="bar-segment high" style={{ width: `${highPct}%` }} title={`High: ${summary.highRiskCount}`} />}
              {medPct > 0 && <div className="bar-segment medium" style={{ width: `${medPct}%` }} title={`Medium: ${summary.mediumRiskCount}`} />}
              {lowPct > 0 && <div className="bar-segment low" style={{ width: `${lowPct}%` }} title={`Low: ${summary.lowRiskCount}`} />}
            </div>
            <div className="bar-legend-row">
              <div className="legend-item"><span className="legend-dot critical"></span>Critical: <strong>{summary.criticalRiskCount || 0}</strong></div>
              <div className="legend-item"><span className="legend-dot high"></span>High: <strong>{summary.highRiskCount || 0}</strong></div>
              <div className="legend-item"><span className="legend-dot medium"></span>Medium: <strong>{summary.mediumRiskCount || 0}</strong></div>
              <div className="legend-item"><span className="legend-dot low"></span>Low: <strong>{summary.lowRiskCount || 0}</strong></div>
            </div>
          </div>
        </div>

        {/* 2. Quantum Exposure */}
        <div className="vis-card">
          <div className="vis-header">
            <span>Quantum Exposure</span>
            <span className="tag-subtle">Primitive Analysis</span>
          </div>
          <div className="status-counts-row">
            <div className="status-count-item">
              <span className="status-count-val" style={{ color: 'var(--quantum-vulnerable)' }}>
                {quantumVulnerableCount}
              </span>
              <span className="status-count-lbl">Quantum Vulnerable</span>
            </div>
            <div className="status-count-item">
              <span className="status-count-val" style={{ color: 'var(--quantum-safe)' }}>
                {quantumSafeCount}
              </span>
              <span className="status-count-lbl">Quantum Resistant / Safe</span>
            </div>
          </div>
        </div>

        {/* 3. Migration Status */}
        <div className="vis-card">
          <div className="vis-header">
            <span>Migration Readiness</span>
            <span className="tag-subtle">NIST PQC Status</span>
          </div>
          <div className="status-counts-row">
            <div className="status-count-item">
              <span className="status-count-val" style={{ color: 'var(--pqc-accent)' }}>
                {summary.pqcRecommendedCount || 0}
              </span>
              <span className="status-count-lbl">Migration Required</span>
            </div>
            <div className="status-count-item">
              <span className="status-count-val" style={{ color: 'var(--text-secondary)' }}>
                {summary.pqcNeedsAnalysisCount || 0}
              </span>
              <span className="status-count-lbl">Needs Analysis</span>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Discovered Assets Table */}
      <div className="card-panel">
        <div className="card-panel-header">
          <div>
            <h3 className="card-panel-title">Discovered Cryptographic Assets Quick View</h3>
            <span className="card-panel-sub">Click on any row to open the complete asset reasoning chain drawer</span>
          </div>
          <button className="btn-secondary btn-sm" onClick={() => onNavigate('inventory')}>
            View Full Inventory ({findings.length}) →
          </button>
        </div>

        <div className="table-container">
          <table className="enterprise-table">
            <thead>
              <tr>
                <th>Algorithm</th>
                <th>Variant</th>
                <th>Purpose</th>
                <th>Key Size</th>
                <th>Risk</th>
                <th>Quantum Status</th>
                <th>Confidence</th>
                <th>Source Location</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {findings.slice(0, 8).map((finding, idx) => {
                const risk = riskAssessments[idx];
                const riskLevel = risk?.riskLevel || 'LOW';
                const quantumRisk = risk?.quantumRisk || 'NONE';

                return (
                  <tr
                    key={idx}
                    className="clickable-row"
                    onClick={() => onSelectFinding(idx)}
                  >
                    <td>
                      <span className="algo-text">{finding.algorithm}</span>
                    </td>
                    <td>
                      {finding.variant ? <span className="tag-subtle">{finding.variant}</span> : <span className="text-muted">—</span>}
                    </td>
                    <td>
                      <span className="tag-subtle">{finding.purpose}</span>
                    </td>
                    <td className="font-mono">
                      {finding.keySize ? `${finding.keySize} bits` : '—'}
                    </td>
                    <td>
                      <span className={`risk-badge ${riskLevel.toLowerCase()}`}>
                        {riskLevel} {risk ? `(${risk.riskScore})` : ''}
                      </span>
                    </td>
                    <td>
                      <span className={`quantum-badge ${quantumRisk.toLowerCase()}`}>
                        {quantumRisk === 'HIGH' ? 'VULNERABLE' : quantumRisk === 'LOW' ? 'SAFE' : quantumRisk}
                      </span>
                    </td>
                    <td>
                      <span className="tag-subtle font-mono">{finding.confidence}</span>
                    </td>
                    <td>
                      <span className="font-mono text-muted" style={{ fontSize: '11px' }} title={finding.file}>
                        {finding.file.split(/[\\/]/).pop()}:{finding.line}
                      </span>
                    </td>
                    <td>
                      <button
                        className="btn-secondary"
                        style={{ padding: '3px 8px', fontSize: '11px' }}
                        onClick={(e) => {
                          e.stopPropagation();
                          onSelectFinding(idx);
                        }}
                      >
                        Inspect
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
