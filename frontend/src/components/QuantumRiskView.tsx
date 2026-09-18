import React, { useState } from 'react';
import type { CryptoFinding, RiskAssessment, ProjectAnalysisContext } from '../types/analysis';

interface QuantumRiskViewProps {
  findings: CryptoFinding[];
  riskAssessments: RiskAssessment[];
  context?: ProjectAnalysisContext;
  onSelectFinding: (index: number) => void;
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

export const QuantumRiskView: React.FC<QuantumRiskViewProps> = ({
  findings,
  riskAssessments,
  context,
  onSelectFinding,
}) => {
  const [selectedContextPreset, setSelectedContextPreset] = useState<string>('Default');

  const migrationTime = context?.migrationTimeYears ?? 3;
  const dataLifetime = context?.dataLifetimeYears ?? 10;
  const threatHorizon = context?.threatHorizonYears ?? 10;
  const totalExposure = migrationTime + dataLifetime;
  const moscaConditionMet = totalExposure > threatHorizon;

  // Quantum aggregations
  const quantumVulnerableFindings = findings
    .map((finding, idx) => ({ finding, risk: riskAssessments[idx], idx }))
    .filter((item) => item.risk?.quantumRisk === 'HIGH' || item.risk?.quantumRiskResult?.quantumVulnerable);

  const totalAssets = findings.length || 1;
  const vulnCount = quantumVulnerableFindings.length;
  const vulnPct = Math.round((vulnCount / totalAssets) * 100);

  // SVG Gauge calculation
  const gaugeRadius = 38;
  const gaugeCircumference = 2 * Math.PI * gaugeRadius;
  const gaugeDash = (vulnPct / 100) * gaugeCircumference;

  // Counts for bar chart
  const critCount = quantumVulnerableFindings.filter((f) => f.risk?.riskLevel === 'CRITICAL').length;
  const highCount = quantumVulnerableFindings.filter((f) => f.risk?.riskLevel === 'HIGH').length;
  const medCount = quantumVulnerableFindings.filter((f) => f.risk?.riskLevel === 'MEDIUM').length;
  const lowCount = quantumVulnerableFindings.filter((f) => f.risk?.riskLevel === 'LOW').length;
  const maxBar = Math.max(1, critCount, highCount, medCount, lowCount);

  return (
    <div>
      {/* Header matching Screen 7 */}
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Quantum Risk Analysis</h1>
          <p className="view-subtitle">Assess quantum vulnerability and migration urgency under Shor&apos;s and Grover&apos;s algorithms</p>
        </div>
        <div className="view-actions">
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Project Context:</span>
            <select
              className="filter-select font-mono"
              value={selectedContextPreset}
              onChange={(e) => setSelectedContextPreset(e.target.value)}
              style={{ minWidth: '120px' }}
            >
              <option value="Default">Default</option>
              <option value="CoreBanking">Core Banking (Y: 15, X: 5, Z: 10)</option>
              <option value="PaymentGateway">Payment Gateway (Y: 10, X: 3, Z: 10)</option>
              <option value="IdentityAuth">Identity Auth (Y: 8, X: 2, Z: 10)</option>
            </select>
          </div>
        </div>
      </div>

      {/* Top Row: Circular Gauge & Mosca Assessment */}
      <div className="quantum-top-grid">
        {/* Card 1: Circular Gauge: Quantum Vulnerable */}
        <div className="chart-card">
          <div className="chart-card-header">
            <h3 className="chart-card-title">Quantum Risk Ratio</h3>
          </div>
          <div className="donut-chart-container" style={{ justifyContent: 'center' }}>
            <div className="donut-svg-wrapper">
              <svg viewBox="0 0 100 100" className="donut-svg">
                <circle cx="50" cy="50" r={gaugeRadius} className="donut-bg" />
                <circle
                  cx="50"
                  cy="50"
                  r={gaugeRadius}
                  className="donut-segment crit"
                  strokeDasharray={`${gaugeDash} ${gaugeCircumference - gaugeDash}`}
                  strokeDashoffset={0}
                />
              </svg>
              <div className="donut-center-text">
                <span className="donut-center-val text-critical">{vulnCount}</span>
                <span className="donut-center-lbl">Quantum Vulnerable</span>
                <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>of {findings.length} assets</span>
              </div>
            </div>
          </div>
        </div>

        {/* Card 2: Mosca Assessment */}
        <div className="chart-card">
          <div className="chart-card-header">
            <div>
              <h3 className="chart-card-title">Mosca&apos;s Theorem: Quantum Risk Condition</h3>
              <span className="chart-card-sub">Migration Time (X) + Data Lifetime (Y) &gt; Threat Horizon (Z)</span>
            </div>
          </div>

          <div className="mosca-four-metrics-grid">
            <div className="mosca-metric-box">
              <span className="m-label">Migration Time (X)</span>
              <span className="m-val">{migrationTime} yrs</span>
            </div>
            <div className="mosca-metric-box">
              <span className="m-label">Data Lifetime (Y)</span>
              <span className="m-val">{dataLifetime} yrs</span>
            </div>
            <div className="mosca-metric-box highlight">
              <span className="m-label">Total Exposure (X+Y)</span>
              <span className="m-val text-critical">{totalExposure} yrs</span>
            </div>
            <div className="mosca-metric-box">
              <span className="m-label">Threat Horizon (Z)</span>
              <span className="m-val">{threatHorizon} yrs</span>
            </div>
          </div>

          <div className={`mosca-status-strip ${moscaConditionMet ? 'alert' : 'safe'}`}>
            <span className="mosca-strip-calc font-mono font-bold">
              {totalExposure} &gt; {threatHorizon}
            </span>
            <span className="mosca-strip-badge font-bold">
              {moscaConditionMet ? '⚠ Migration Required' : '✓ Migration Not Required'}
            </span>
          </div>
        </div>
      </div>

      {/* Bottom Row: Quantum Risk Distribution & Migration Urgency */}
      <div className="dashboard-charts-grid" style={{ marginTop: '20px' }}>
        {/* Left Chart: Quantum Risk Distribution */}
        <div className="chart-card">
          <div className="chart-card-header">
            <h3 className="chart-card-title">Quantum Risk Distribution</h3>
          </div>
          <div className="vertical-bar-chart-container">
            <div className="vbar-group">
              <span className="vbar-count">{critCount}</span>
              <div className="vbar-track">
                <div
                  className="vbar-fill critical"
                  style={{ height: `${Math.max(10, (critCount / maxBar) * 100)}%` }}
                ></div>
              </div>
              <span className="vbar-label">Critical</span>
            </div>

            <div className="vbar-group">
              <span className="vbar-count">{highCount}</span>
              <div className="vbar-track">
                <div
                  className="vbar-fill high"
                  style={{ height: `${Math.max(10, (highCount / maxBar) * 100)}%` }}
                ></div>
              </div>
              <span className="vbar-label">High</span>
            </div>

            <div className="vbar-group">
              <span className="vbar-count">{medCount}</span>
              <div className="vbar-track">
                <div
                  className="vbar-fill medium"
                  style={{ height: `${Math.max(10, (medCount / maxBar) * 100)}%` }}
                ></div>
              </div>
              <span className="vbar-label">Medium</span>
            </div>

            <div className="vbar-group">
              <span className="vbar-count">{lowCount}</span>
              <div className="vbar-track">
                <div
                  className="vbar-fill low"
                  style={{ height: `${Math.max(10, (lowCount / maxBar) * 100)}%` }}
                ></div>
              </div>
              <span className="vbar-label">Low</span>
            </div>
          </div>
        </div>

        {/* Right Card: Migration Urgency */}
        <div className="chart-card">
          <div className="chart-card-header">
            <h3 className="chart-card-title">Migration Urgency</h3>
            <span className="chart-card-sub">Based on business criticality and data sensitivity</span>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '12px' }}>
            <div style={{ padding: '14px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <strong style={{ fontSize: '13.5px', color: 'var(--text-primary)' }}>High / Critical Urgency</strong>
                <p style={{ fontSize: '11.5px', color: 'var(--text-secondary)', marginTop: '2px' }}>
                  Public key algorithms subject to Store Now, Decrypt Later (SNDL) attacks
                </p>
              </div>
              <span className="risk-badge critical">{vulnCount} Assets</span>
            </div>

            <div style={{ padding: '14px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <strong style={{ fontSize: '13.5px', color: 'var(--text-primary)' }}>Standard Governance</strong>
                <p style={{ fontSize: '11.5px', color: 'var(--text-secondary)', marginTop: '2px' }}>
                  Symmetric and hash primitives requiring key size evaluation
                </p>
              </div>
              <span className="risk-badge low">{Math.max(0, findings.length - vulnCount)} Assets</span>
            </div>
          </div>
        </div>
      </div>

      {/* Quantum Vulnerable Assets Table */}
      <div className="card-panel" style={{ marginTop: '20px' }}>
        <div className="card-panel-header">
          <div>
            <h3 className="card-panel-title">Quantum-Vulnerable Cryptographic Primitives</h3>
            <span className="card-panel-sub">Discovered public-key primitives subject to Shor&apos;s polynomial-time cryptanalysis</span>
          </div>
        </div>

        <div className="table-container">
          <table className="enterprise-table">
            <thead>
              <tr>
                <th style={{ width: '22%' }}>Algorithm</th>
                <th style={{ width: '22%' }}>Purpose</th>
                <th style={{ width: '14%' }}>Risk Level</th>
                <th style={{ width: '22%' }}>Quantum Threat</th>
                <th style={{ width: '12%' }}>Location</th>
                <th style={{ width: '8%', textAlign: 'right' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {quantumVulnerableFindings.length === 0 ? (
                <tr>
                  <td colSpan={6} style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                    No quantum-vulnerable public key primitives detected.
                  </td>
                </tr>
              ) : (
                quantumVulnerableFindings.map(({ finding, risk, idx }) => {
                  const riskLevel = risk?.riskLevel || 'HIGH';
                  const purposeDisplay = formatTitleCase(finding.purpose);

                  return (
                    <tr
                      key={idx}
                      className="clickable-row"
                      onClick={() => onSelectFinding(idx)}
                    >
                      <td>
                        <div className="algo-cell-block">
                          <span className="algo-primary-title">{finding.algorithm}</span>
                          {(finding.keySize || (finding.variant && finding.variant !== finding.algorithm)) && (
                            <span className="algo-secondary-sub font-mono">
                              {finding.keySize ? `${finding.keySize} bits` : ''}
                              {finding.keySize && finding.variant && finding.variant !== finding.algorithm ? ' · ' : ''}
                              {finding.variant && finding.variant !== finding.algorithm ? finding.variant : ''}
                            </span>
                          )}
                        </div>
                      </td>
                      <td>
                        <span className="table-purpose-text">{purposeDisplay}</span>
                      </td>
                      <td>
                        <span className={`risk-badge ${riskLevel.toLowerCase()}`}>
                          {riskLevel}
                        </span>
                      </td>
                      <td>
                        <span className="table-threat-text">
                          Shor&apos;s Discrete Log / Factoring
                        </span>
                      </td>
                      <td>
                        <div className="source-location-cell font-mono">
                          <span className="source-file">{finding.file.split(/[\\/]/).pop()}</span>
                          <span className="source-line">:{finding.line}</span>
                        </div>
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <button
                          className="btn-secondary btn-sm"
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
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
