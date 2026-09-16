import React from 'react';
import type { CryptoFinding, RiskAssessment, ProjectAnalysisContext } from '../types/analysis';

interface QuantumRiskViewProps {
  findings: CryptoFinding[];
  riskAssessments: RiskAssessment[];
  context?: ProjectAnalysisContext;
  onSelectFinding: (index: number) => void;
}

export const QuantumRiskView: React.FC<QuantumRiskViewProps> = ({
  findings,
  riskAssessments,
  context,
  onSelectFinding,
}) => {
  const migrationTime = context?.migrationTimeYears ?? 3;
  const dataLifetime = context?.dataLifetimeYears ?? 10;
  const threatHorizon = context?.threatHorizonYears ?? 10;
  const totalExposure = migrationTime + dataLifetime;
  const moscaConditionMet = totalExposure > threatHorizon;

  // Quantum aggregations
  const quantumVulnerableFindings = findings
    .map((finding, idx) => ({ finding, risk: riskAssessments[idx], idx }))
    .filter((item) => item.risk?.quantumRisk === 'HIGH' || item.risk?.quantumRiskResult?.quantumVulnerable);

  const criticalQuantumCount = quantumVulnerableFindings.filter(
    (item) => item.risk?.riskLevel === 'CRITICAL' || item.risk?.quantumRiskResult?.migrationUrgency === 'CRITICAL'
  ).length;

  const migrationRequiredCount = quantumVulnerableFindings.filter(
    (item) => item.risk?.quantumRiskResult?.migrationRequired !== false
  ).length;

  return (
    <div>
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">Quantum Risk &amp; Exposure Assessment</h1>
          <p className="view-subtitle">
            Evaluation of asymmetric and symmetric cryptographic exposure against Shor's and Grover's quantum cryptanalytic threats using Mosca's Theorem.
          </p>
        </div>
      </div>

      {/* Quantum Metric Summary */}
      <div className="metrics-grid">
        <div className="metric-card quantum">
          <div className="metric-card-top">
            <span className="metric-card-label">Quantum Vulnerable Assets</span>
            <span className="metric-card-icon" style={{ color: 'var(--risk-high)' }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="2" y1="12" x2="22" y2="12" />
              </svg>
            </span>
          </div>
          <div className="metric-card-val">{quantumVulnerableFindings.length}</div>
          <div className="metric-card-footer">Public-key / Asymmetric primitives</div>
        </div>

        <div className="metric-card critical">
          <div className="metric-card-top">
            <span className="metric-card-label">Critical Quantum Assets</span>
            <span className="metric-card-icon" style={{ color: 'var(--risk-critical)' }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
              </svg>
            </span>
          </div>
          <div className="metric-card-val">{criticalQuantumCount}</div>
          <div className="metric-card-footer">Elevated urgency classification</div>
        </div>

        <div className="metric-card pqc">
          <div className="metric-card-top">
            <span className="metric-card-label">Migration Required</span>
            <span className="metric-card-icon" style={{ color: 'var(--pqc-accent)' }}>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="16 3 21 3 21 8" />
                <line x1="4" y1="20" x2="21" y2="3" />
              </svg>
            </span>
          </div>
          <div className="metric-card-val">{migrationRequiredCount}</div>
          <div className="metric-card-footer">Mosca inequality satisfied</div>
        </div>

        <div className="metric-card">
          <div className="metric-card-top">
            <span className="metric-card-label">Threat Horizon</span>
            <span className="metric-card-icon">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
            </span>
          </div>
          <div className="metric-card-val">{threatHorizon} yrs</div>
          <div className="metric-card-footer">Estimated cryptanalytic horizon</div>
        </div>

        <div className="metric-card">
          <div className="metric-card-top">
            <span className="metric-card-label">Total Exposure</span>
            <span className="metric-card-icon">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              </svg>
            </span>
          </div>
          <div className="metric-card-val font-mono">{totalExposure} yrs</div>
          <div className="metric-card-footer">Migration Time + Data Lifetime</div>
        </div>
      </div>

      {/* Mosca Theorem Mathematical Panel */}
      <div className="mosca-panel">
        <div className="mosca-panel-header">
          <div>
            <h3 className="card-panel-title">Mosca's Theorem: Quantum Risk Condition</h3>
            <span className="card-panel-sub">Mathematical formula evaluating post-quantum cryptographic urgency</span>
          </div>
          <span className={`mosca-verdict-tag ${moscaConditionMet ? 'required' : 'safe'}`}>
            {moscaConditionMet ? 'MIGRATION REQUIRED' : 'MIGRATION NOT REQUIRED'}
          </span>
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
              <span className="term-label">Total Exposure</span>
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
              Business Criticality: <code>{context?.businessCriticality || 'CRITICAL'}</code>
            </div>
          </div>
        </div>

        <p className="mosca-explanation-text">
          Mosca's inequality states that if the time required to migrate infrastructure (X) plus the duration that data must remain secret (Y) exceeds the time until a cryptographically relevant quantum computer exists (Z), encrypted data is already at risk of "Store Now, Decrypt Later" (SNDL) adversary attacks.
        </p>
      </div>

      {/* Quantum Vulnerable Assets Table */}
      <div className="card-panel">
        <div className="card-panel-header">
          <div>
            <h3 className="card-panel-title">Quantum-Vulnerable Cryptographic Primitives</h3>
            <span className="card-panel-sub">
              Discovered public-key mechanisms and legacy primitives requiring post-quantum replacement
            </span>
          </div>
        </div>

        <div className="table-container">
          <table className="enterprise-table">
            <thead>
              <tr>
                <th>Algorithm</th>
                <th>Variant</th>
                <th>Purpose</th>
                <th>Key Size</th>
                <th>Risk Level</th>
                <th>Mosca Urgency</th>
                <th>Threat Model</th>
                <th>Location</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {quantumVulnerableFindings.length === 0 ? (
                <tr>
                  <td colSpan={9} style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                    No quantum-vulnerable public key primitives detected in current analysis.
                  </td>
                </tr>
              ) : (
                quantumVulnerableFindings.map(({ finding, risk, idx }) => {
                  const riskLevel = risk?.riskLevel || 'HIGH';
                  const urgency = risk?.quantumRiskResult?.migrationUrgency || 'HIGH';

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
                        <span className={`risk-badge ${urgency.toLowerCase()}`}>
                          {urgency}
                        </span>
                      </td>
                      <td>
                        <span className="tag-subtle" style={{ color: 'var(--risk-high)' }}>
                          Shor's Discrete Log / Factoring
                        </span>
                      </td>
                      <td>
                        <span className="font-mono text-muted" style={{ fontSize: '11px' }}>
                          {finding.file.split(/[\\/]/).pop()}:{finding.line}
                        </span>
                      </td>
                      <td>
                        <button
                          className="btn-secondary btn-sm"
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
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
