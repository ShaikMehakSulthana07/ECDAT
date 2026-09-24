import React, { useState, useEffect } from 'react';
import type { CryptoFinding, RiskAssessment, PQCRecommendation } from '../types/analysis';

interface AssetDetailDrawerProps {
  finding: CryptoFinding;
  riskAssessment?: RiskAssessment;
  pqcRecommendation?: PQCRecommendation;
  findingIndex: number;
  totalFindings: number;
  onClose: () => void;
  onNavigatePrev?: () => void;
  onNavigateNext?: () => void;
}

export const AssetDetailDrawer: React.FC<AssetDetailDrawerProps> = ({
  finding,
  riskAssessment,
  pqcRecommendation,
  findingIndex,
  totalFindings,
  onClose,
  onNavigatePrev,
  onNavigateNext,
}) => {
  const [activeTab, setActiveTab] = useState<'overview' | 'mosca' | 'pqc' | 'evidence'>('overview');

  const riskLevel = riskAssessment?.riskLevel || 'LOW';
  const isQuantumVuln = riskAssessment?.quantumRisk === 'HIGH' || riskAssessment?.quantumRiskResult?.quantumVulnerable;
  const assetCategory = finding.assetCategory || 'UNKNOWN';
  const lifecycleStatus = finding.lifecycleStatus || 'UNKNOWN';
  const quantumResult = riskAssessment?.quantumRiskResult;

  // Keyboard shortcut listener for Esc, ArrowLeft, ArrowRight
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        onClose();
      } else if (e.key === 'ArrowLeft' && onNavigatePrev) {
        onNavigatePrev();
      } else if (e.key === 'ArrowRight' && onNavigateNext) {
        onNavigateNext();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [onClose, onNavigatePrev, onNavigateNext]);

  return (
    <div className="drawer-backdrop" onClick={onClose}>
      <div className="drawer-panel" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
        {/* Drawer Header with Back link */}
        <div className="drawer-header">
          <div className="drawer-title-group">
            <button className="back-link-btn" onClick={onClose}>
              ← Back to Inventory
            </button>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '4px' }}>
              <span className="drawer-tag">Asset Finding {findingIndex + 1} of {totalFindings}</span>
              <h2 className="drawer-title">Asset Detail</h2>
            </div>
          </div>

          <div className="drawer-nav-actions">
            {onNavigatePrev && (
              <button className="btn-secondary btn-sm" onClick={onNavigatePrev} title="Previous asset (Left Arrow)">
                ←
              </button>
            )}
            {onNavigateNext && (
              <button className="btn-secondary btn-sm" onClick={onNavigateNext} title="Next asset (Right Arrow)">
                →
              </button>
            )}
            <button className="drawer-close-btn" onClick={onClose} title="Close drawer (Esc)">
              ✕
            </button>
          </div>
        </div>

        {/* Drawer Body */}
        <div className="drawer-body">
          {/* Top Asset Hero Summary (Screen 5 Reference) */}
          <div className="asset-hero-card">
            <div className="asset-hero-left">
              <div className="asset-hero-name">
                {finding.algorithm} {finding.keySize ? `-${finding.keySize}` : ''}
              </div>
              <div className="asset-hero-purpose">{finding.purpose.replace(/_/g, ' ')}</div>
              <div className="asset-hero-badges">
                <span className={`risk-badge ${riskLevel.toLowerCase()}`}>
                  {riskLevel} Risk
                </span>
                <span className={`quantum-badge ${isQuantumVuln ? 'vulnerable' : 'safe'}`}>
                  {isQuantumVuln ? 'Quantum Vulnerable' : 'Quantum Safe'}
                </span>
              </div>
            </div>

            <div className="asset-hero-right">
              <div className="asset-hero-meta-row">
                <span className="meta-lbl">Source Location</span>
                <span className="meta-val font-mono" title={finding.file}>
                  {finding.file.split(/[\\/]/).pop()} : {finding.line}
                </span>
              </div>
              <div className="asset-hero-meta-row" style={{ marginTop: '6px' }}>
                <span className="meta-lbl">Confidence</span>
                <span className="meta-val font-mono">{finding.confidence}</span>
              </div>
            </div>
          </div>

          {/* Reasoning Chain Flow */}
          <div className="reasoning-chain-box">
            <div className="chain-title">Reasoning Chain &amp; Cryptographic Traceability</div>
            <div className="chain-nodes">
              <div className="chain-node">
                <span className="chain-node-step">1. Source AST</span>
                <span className="chain-node-val">{finding.file.split(/[\\/]/).pop()}</span>
                <span className="font-mono text-muted" style={{ fontSize: '10px' }}>L:{finding.line}</span>
              </div>
              <span className="chain-arrow">→</span>
              <div className="chain-node">
                <span className="chain-node-step">2. Discovery</span>
                <span className="chain-node-val">{finding.algorithm}</span>
                <span className="text-muted" style={{ fontSize: '10px' }}>{assetCategory.replace('_', ' ')}</span>
              </div>
              <span className="chain-arrow">→</span>
              <div className="chain-node">
                <span className="chain-node-step">3. Risk Assessment</span>
                <span className={`chain-node-val text-${riskLevel.toLowerCase()}`}>
                  {riskLevel} ({riskAssessment?.riskScore || 0})
                </span>
              </div>
              <span className="chain-arrow">→</span>
              <div className="chain-node">
                <span className="chain-node-step">4. Quantum Assessment</span>
                <span className="chain-node-val">
                  {quantumResult?.quantumVulnerable ? 'VULNERABLE' : 'RESISTANT'}
                </span>
              </div>
              <span className="chain-arrow">→</span>
              <div className="chain-node">
                <span className="chain-node-step">5. PQC Target</span>
                <span className="chain-node-val text-pqc">
                  {pqcRecommendation?.recommendedAlgorithm || 'NEEDS ANALYSIS'}
                </span>
              </div>
            </div>
          </div>

          {/* Tab Navigation (Overview, Mosca Assessment, PQC Recommendation, Evidence) */}
          <div className="asset-detail-tabs">
            <button
              className={`asset-tab-btn ${activeTab === 'overview' ? 'active' : ''}`}
              onClick={() => setActiveTab('overview')}
            >
              Overview
            </button>
            <button
              className={`asset-tab-btn ${activeTab === 'mosca' ? 'active' : ''}`}
              onClick={() => setActiveTab('mosca')}
            >
              Mosca Assessment
            </button>
            <button
              className={`asset-tab-btn ${activeTab === 'pqc' ? 'active' : ''}`}
              onClick={() => setActiveTab('pqc')}
            >
              PQC Recommendation
            </button>
            <button
              className={`asset-tab-btn ${activeTab === 'evidence' ? 'active' : ''}`}
              onClick={() => setActiveTab('evidence')}
            >
              Evidence
            </button>
          </div>

          {/* TAB 1: OVERVIEW */}
          {activeTab === 'overview' && (
            <div className="tab-content-pane">
              <div className="asset-two-col-grid">
                {/* Left Column: Properties */}
                <div className="asset-col-box">
                  <div className="asset-prop-row">
                    <span className="asset-prop-label">Algorithm</span>
                    <span className="asset-prop-value font-bold">{finding.algorithm}</span>
                  </div>
                  <div className="asset-prop-row">
                    <span className="asset-prop-label">Variant</span>
                    <span className="asset-prop-value">{finding.variant || 'Not Specified'}</span>
                  </div>
                  {finding.mode && (
                    <div className="asset-prop-row">
                      <span className="asset-prop-label">Cipher Mode</span>
                      <span className="asset-prop-value font-mono">{finding.mode}</span>
                    </div>
                  )}
                  {finding.padding && (
                    <div className="asset-prop-row">
                      <span className="asset-prop-label">Padding</span>
                      <span className="asset-prop-value font-mono">{finding.padding}</span>
                    </div>
                  )}
                  <div className="asset-prop-row">
                    <span className="asset-prop-label">Purpose</span>
                    <span className="asset-prop-value"><span className="tag-subtle">{finding.purpose.replace(/_/g, ' ')}</span></span>
                  </div>
                  <div className="asset-prop-row">
                    <span className="asset-prop-label">Key Size</span>
                    <span className="asset-prop-value font-mono">
                      {finding.keySize ? `${finding.keySize} bits` : 'Not Inferred'}
                    </span>
                  </div>
                  <div className="asset-prop-row">
                    <span className="asset-prop-label">Category</span>
                    <span className="asset-prop-value"><span className="tag-subtle">{assetCategory.replace(/_/g, ' ')}</span></span>
                  </div>
                  <div className="asset-prop-row">
                    <span className="asset-prop-label">Lifecycle Status</span>
                    <span className="asset-prop-value">
                      <span className={`risk-badge ${lifecycleStatus === 'DEPRECATED' ? 'critical' : 'low'}`}>
                        {lifecycleStatus}
                      </span>
                    </span>
                  </div>
                </div>

                {/* Right Column: Risk & Context */}
                <div className="asset-col-box">
                  <div className="asset-prop-row">
                    <span className="asset-prop-label">Risk Level</span>
                    <span className="asset-prop-value">
                      <span className={`risk-badge ${riskLevel.toLowerCase()}`}>
                        {riskLevel} ({riskAssessment?.riskScore ?? 0}/100)
                      </span>
                    </span>
                  </div>
                  <div className="asset-prop-row">
                    <span className="asset-prop-label">Quantum Status</span>
                    <span className="asset-prop-value">
                      <span className={`quantum-badge ${isQuantumVuln ? 'vulnerable' : 'safe'}`}>
                        {isQuantumVuln ? 'Vulnerable' : 'Safe'}
                      </span>
                    </span>
                  </div>
                  <div className="asset-prop-row">
                    <span className="asset-prop-label">Business Criticality</span>
                    <span className="asset-prop-value">
                      <span className="risk-badge critical">
                        {finding.businessCriticality || quantumResult?.businessCriticality || 'CRITICAL'}
                      </span>
                    </span>
                  </div>
                  <div className="asset-prop-row">
                    <span className="asset-prop-label">Data Sensitivity</span>
                    <span className="asset-prop-value">
                      <span className="tag-subtle" style={{ color: 'var(--risk-high)', borderColor: 'var(--risk-high-border)' }}>
                        {finding.dataSensitivity || quantumResult?.dataSensitivity || 'HIGHLY SENSITIVE'}
                      </span>
                    </span>
                  </div>
                  <div className="asset-prop-row">
                    <span className="asset-prop-label">Library</span>
                    <span className="asset-prop-value">{finding.library || 'Java Cryptography Architecture (JCA)'}</span>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* TAB 2: MOSCA ASSESSMENT */}
          {activeTab === 'mosca' && (
            <div className="tab-content-pane">
              {quantumResult ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  <div className="mosca-formula-container" style={{ padding: '16px' }}>
                    <div className="mosca-equation-block">
                      <div className="mosca-term">
                        <span className="term-label">Migration Time (X)</span>
                        <span className="term-val">{quantumResult.migrationTimeYears} yrs</span>
                      </div>
                      <span className="equation-operator">+</span>
                      <div className="mosca-term">
                        <span className="term-label">Data Lifetime (Y)</span>
                        <span className="term-val">{quantumResult.dataLifetimeYears} yrs</span>
                      </div>
                      <span className="equation-operator">=</span>
                      <div className="mosca-term">
                        <span className="term-label">Total Exposure (X+Y)</span>
                        <span className="term-val font-mono" style={{ color: 'var(--risk-critical)' }}>
                          {quantumResult.totalExposureYears} yrs
                        </span>
                      </div>
                      <span className="equation-operator">&gt;</span>
                      <div className="mosca-term">
                        <span className="term-label">Threat Horizon (Z)</span>
                        <span className="term-val font-mono">{quantumResult.threatHorizonYears} yrs</span>
                      </div>
                    </div>
                  </div>

                  <div className="mosca-verdict-box" style={{ marginTop: '6px' }}>
                    <span className={`mosca-verdict-tag ${quantumResult.migrationRequired ? 'required' : 'safe'}`}>
                      {quantumResult.migrationRequired ? 'MIGRATION REQUIRED' : 'MIGRATION NOT REQUIRED'}
                    </span>
                  </div>

                  <p className="mosca-explanation-text">
                    {quantumResult.explanation}
                  </p>

                  <div style={{ marginTop: '8px' }}>
                    <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-secondary)' }}>
                      Calculation Formula Details:
                    </span>
                    <pre className="font-mono" style={{ fontSize: '11px', background: 'var(--bg-surface-subtle)', padding: '10px', borderRadius: '4px', marginTop: '6px' }}>
                      {quantumResult.calculationDetails}
                    </pre>
                  </div>
                </div>
              ) : (
                <p className="text-muted" style={{ fontSize: '13px' }}>
                  Standard quantum risk assessment evaluated based on asymmetric key structure.
                </p>
              )}
            </div>
          )}

          {/* TAB 3: PQC RECOMMENDATION */}
          {activeTab === 'pqc' && (
            <div className="tab-content-pane">
              {pqcRecommendation ? (
                <table className="props-table">
                  <tbody>
                    <tr>
                      <th>Status</th>
                      <td>
                        <span className={`pqc-status-badge ${pqcRecommendation.recommendationStatus.toLowerCase()}`}>
                          {pqcRecommendation.recommendationStatus}
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <th>Recommended PQC Target</th>
                      <td>
                        <strong className="font-mono" style={{ color: 'var(--pqc-accent)', fontSize: '14px' }}>
                          {pqcRecommendation.recommendedAlgorithm || 'Needs Analysis'}
                        </strong>
                      </td>
                    </tr>
                    {pqcRecommendation.alternativeAlgorithms && pqcRecommendation.alternativeAlgorithms.length > 0 && (
                      <tr>
                        <th>Alternative Standards</th>
                        <td>
                          <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                            {pqcRecommendation.alternativeAlgorithms.map((alt) => (
                              <span key={alt} className="tag-subtle font-mono">{alt}</span>
                            ))}
                          </div>
                        </td>
                      </tr>
                    )}
                    <tr>
                      <th>Migration Strategy</th>
                      <td>
                        <span className="tag-subtle font-mono">{pqcRecommendation.migrationStrategy || 'DIRECT_PQC'}</span>
                      </td>
                    </tr>
                    <tr>
                      <th>Migration Priority</th>
                      <td>
                        <span className={`risk-badge ${pqcRecommendation.migrationPriority.toLowerCase()}`}>
                          {pqcRecommendation.migrationPriority}
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <th>Cryptographic Rationale</th>
                      <td style={{ lineHeight: 1.5 }}>{pqcRecommendation.rationale}</td>
                    </tr>
                  </tbody>
                </table>
              ) : (
                <p className="text-muted" style={{ fontSize: '13px' }}>
                  No dedicated post-quantum recommendation required for this primitive.
                </p>
              )}
            </div>
          )}

          {/* TAB 4: EVIDENCE */}
          {activeTab === 'evidence' && (
            <div className="tab-content-pane">
              <div className="code-evidence-container">
                <div className="code-evidence-header">
                  <span>{finding.file}</span>
                  <span>Line: {finding.line}</span>
                </div>
                <pre className="code-evidence-body">
                  <code>{finding.evidence}</code>
                </pre>
              </div>
            </div>
          )}
        </div>

        {/* Drawer Footer */}
        <div className="drawer-footer">
          <span>
            Use <kbd style={{ padding: '2px 4px', background: 'var(--bg-surface-subtle)', border: '1px solid var(--border-default)', borderRadius: '3px' }}>Esc</kbd> or click outside to exit.
          </span>
          <button className="btn-secondary" onClick={onClose}>
            Close Drawer
          </button>
        </div>
      </div>
    </div>
  );
};
