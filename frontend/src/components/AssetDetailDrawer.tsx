import React, { useEffect } from 'react';
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
  const riskLevel = riskAssessment?.riskLevel || 'LOW';
  const quantumRisk = riskAssessment?.quantumRisk || 'NONE';
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
        {/* Drawer Header */}
        <div className="drawer-header">
          <div className="drawer-title-group">
            <span className="drawer-tag">Asset Finding {findingIndex + 1} of {totalFindings}</span>
            <h2 className="drawer-title">
              {finding.algorithm} {finding.variant ? `(${finding.variant})` : ''}
            </h2>
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
          {/* Reasoning Chain Visualization */}
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
                <span className="text-muted" style={{ fontSize: '10px' }}>Quantum: {quantumRisk}</span>
              </div>
              <span className="chain-arrow">→</span>
              <div className="chain-node">
                <span className="chain-node-step">4. Quantum Assessment</span>
                <span className="chain-node-val">
                  {quantumResult?.quantumVulnerable ? 'VULNERABLE' : 'RESISTANT'}
                </span>
                <span className="text-muted" style={{ fontSize: '10px' }}>
                  {quantumResult?.migrationRequired ? 'MIGRATE' : 'SAFE'}
                </span>
              </div>
              <span className="chain-arrow">→</span>
              <div className="chain-node">
                <span className="chain-node-step">5. PQC Target</span>
                <span className="chain-node-val text-pqc">
                  {pqcRecommendation?.recommendedAlgorithm || 'NEEDS ANALYSIS'}
                </span>
                <span className="text-muted" style={{ fontSize: '10px' }}>
                  {pqcRecommendation?.migrationPriority || 'MEDIUM'}
                </span>
              </div>
            </div>
          </div>

          {/* Section 1: Cryptographic Asset Details */}
          <div className="drawer-section">
            <h3 className="drawer-section-heading">Cryptographic Asset Properties</h3>
            <table className="props-table">
              <tbody>
                <tr>
                  <th>Algorithm</th>
                  <td className="font-bold">{finding.algorithm}</td>
                </tr>
                <tr>
                  <th>Variant / Mode</th>
                  <td>{finding.variant || 'Not Specified'}</td>
                </tr>
                <tr>
                  <th>Declared Purpose</th>
                  <td><span className="tag-subtle">{finding.purpose}</span></td>
                </tr>
                <tr>
                  <th>Key Size</th>
                  <td className="font-mono">
                    {finding.keySize ? `${finding.keySize} bits` : 'Not Statically Inferred'}
                  </td>
                </tr>
                <tr>
                  <th>Asset Category</th>
                  <td><span className="tag-subtle">{assetCategory.replace('_', ' ')}</span></td>
                </tr>
                <tr>
                  <th>Lifecycle Status</th>
                  <td>
                    <span className={`risk-badge ${lifecycleStatus === 'DEPRECATED' ? 'critical' : lifecycleStatus === 'ACTIVE' ? 'low' : 'medium'}`}>
                      {lifecycleStatus}
                    </span>
                  </td>
                </tr>
                <tr>
                  <th>Usage Category</th>
                  <td><span className="tag-subtle">{finding.usageCategory || 'DIRECT_USAGE'}</span></td>
                </tr>
                <tr>
                  <th>Cryptographic Library</th>
                  <td>{finding.library || 'Java Cryptography Architecture (JCA)'}</td>
                </tr>
                {finding.protocol && (
                  <tr>
                    <th>Protocol Version</th>
                    <td className="font-mono">{finding.protocol}</td>
                  </tr>
                )}
                <tr>
                  <th>Detection Confidence</th>
                  <td>
                    <span className={`tag-subtle font-mono ${finding.confidence.toLowerCase()}`}>
                      {finding.confidence}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          {/* Section 2: Source Code Evidence */}
          <div className="drawer-section">
            <h3 className="drawer-section-heading">Source Code Evidence</h3>
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

          {/* Section 3: Quantum Assessment (Mosca Condition) */}
          <div className="drawer-section">
            <h3 className="drawer-section-heading">Quantum Exposure &amp; Mosca Assessment</h3>
            {quantumResult ? (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div
                  style={{
                    backgroundColor: 'var(--bg-surface-subtle)',
                    border: '1px solid var(--border-default)',
                    borderRadius: 'var(--radius-sm)',
                    padding: '14px',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '10px' }}>
                    <span style={{ fontSize: '11px', color: 'var(--text-muted)', textTransform: 'uppercase' }}>
                      Mosca Equation Calculation
                    </span>
                    <span
                      className={`risk-badge ${quantumResult.migrationRequired ? 'critical' : 'low'}`}
                    >
                      {quantumResult.migrationRequired ? 'MIGRATION REQUIRED' : 'MIGRATION NOT REQUIRED'}
                    </span>
                  </div>

                  <div className="font-mono" style={{ fontSize: '13px', color: 'var(--text-primary)', marginBottom: '8px' }}>
                    {quantumResult.migrationTimeYears} yr (Migration) + {quantumResult.dataLifetimeYears} yr (Data Lifetime) = {quantumResult.totalExposureYears} yr (Exposure)
                    {quantumResult.moscaConditionMet ? (
                      <span style={{ color: 'var(--risk-critical)', fontWeight: 'bold' }}> &gt; {quantumResult.threatHorizonYears} yr (Threat Horizon)</span>
                    ) : (
                      <span style={{ color: 'var(--risk-low)' }}> ≤ {quantumResult.threatHorizonYears} yr (Threat Horizon)</span>
                    )}
                  </div>

                  <p style={{ fontSize: '12px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
                    {quantumResult.explanation}
                  </p>
                </div>

                <table className="props-table">
                  <tbody>
                    <tr>
                      <th>Quantum Vulnerable</th>
                      <td>
                        <span className={`quantum-badge ${quantumResult.quantumVulnerable ? 'vulnerable' : 'safe'}`}>
                          {quantumResult.quantumVulnerable ? 'YES' : 'NO'}
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <th>Migration Urgency</th>
                      <td>
                        <span className={`risk-badge ${quantumResult.migrationUrgency.toLowerCase()}`}>
                          {quantumResult.migrationUrgency}
                        </span>
                      </td>
                    </tr>
                    <tr>
                      <th>Calculation Details</th>
                      <td>
                        <pre className="font-mono" style={{ fontSize: '11px', color: 'var(--text-secondary)', whiteSpace: 'pre-wrap' }}>
                          {quantumResult.calculationDetails}
                        </pre>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            ) : (
              <p className="text-muted" style={{ fontSize: '12px' }}>
                Standard quantum risk assessment evaluated based on asymmetric key structure.
              </p>
            )}
          </div>

          {/* Section 4: Business Context */}
          <div className="drawer-section">
            <h3 className="drawer-section-heading">Business Context &amp; Data Classification</h3>
            <table className="props-table">
              <tbody>
                <tr>
                  <th>Business Criticality</th>
                  <td>
                    <span className="tag-subtle">
                      {finding.businessCriticality || quantumResult?.businessCriticality || 'UNKNOWN'}
                    </span>
                  </td>
                </tr>
                <tr>
                  <th>Data Sensitivity</th>
                  <td>
                    <span className="tag-subtle">
                      {finding.dataSensitivity || quantumResult?.dataSensitivity || 'UNKNOWN'}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          {/* Section 5: PQC Migration Recommendation */}
          {pqcRecommendation && (
            <div className="drawer-section">
              <h3 className="drawer-section-heading">Post-Quantum Cryptography Migration Target</h3>
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
                    <th>Recommended PQC</th>
                    <td>
                      {pqcRecommendation.recommendedAlgorithm ? (
                        <span className="font-bold font-mono" style={{ color: 'var(--pqc-accent)' }}>
                          {pqcRecommendation.recommendedAlgorithm}
                        </span>
                      ) : (
                        <span className="text-muted">None directly standardized / Needs Analysis</span>
                      )}
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
                      <span className="tag-subtle font-mono">
                        {pqcRecommendation.migrationStrategy || (pqcRecommendation.recommendedAlgorithm ? 'DIRECT_PQC' : 'NEEDS_ANALYSIS')}
                      </span>
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

              {pqcRecommendation.considerations && pqcRecommendation.considerations.length > 0 && (
                <div style={{ marginTop: '10px' }}>
                  <span style={{ fontSize: '11px', fontWeight: 600, color: 'var(--text-secondary)' }}>
                    Engineering &amp; Protocol Considerations:
                  </span>
                  <ul style={{ paddingLeft: '18px', marginTop: '6px', fontSize: '12px', color: 'var(--text-secondary)', display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    {pqcRecommendation.considerations.map((c, idx) => (
                      <li key={idx}>{c}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          )}

          {/* Section 6: Risk Factors */}
          {riskAssessment && riskAssessment.factors && riskAssessment.factors.length > 0 && (
            <div className="drawer-section">
              <h3 className="drawer-section-heading">Detailed Risk Factors</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {riskAssessment.factors.map((f, idx) => (
                  <div
                    key={idx}
                    style={{
                      padding: '10px 12px',
                      backgroundColor: 'var(--bg-surface-subtle)',
                      border: '1px solid var(--border-subtle)',
                      borderRadius: 'var(--radius-sm)',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px', fontSize: '12px' }}>
                      <strong className="font-mono">{f.name}</strong>
                      <span className="font-mono" style={{ color: 'var(--risk-high)' }}>+{f.score} pts</span>
                    </div>
                    <p style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{f.explanation}</p>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Drawer Footer */}
        <div className="drawer-footer">
          <span>
            Use <kbd style={{ padding: '2px 4px', background: 'var(--bg-surface-subtle)', border: '1px solid var(--border-default)', borderRadius: '3px' }}>Esc</kbd> or click outside to exit inspection.
          </span>
          <button className="btn-secondary" onClick={onClose}>
            Close Drawer
          </button>
        </div>
      </div>
    </div>
  );
};
