import React from 'react';
import type { CryptoFinding, RiskAssessment, PQCRecommendation } from '../types/analysis';

interface FindingDetailsModalProps {
  finding: CryptoFinding;
  riskAssessment?: RiskAssessment;
  pqcRecommendation?: PQCRecommendation;
  findingIndex: number;
  totalFindings: number;
  onClose: () => void;
  onNavigatePrev?: () => void;
  onNavigateNext?: () => void;
}

export const FindingDetailsModal: React.FC<FindingDetailsModalProps> = ({
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

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-container" onClick={(e) => e.stopPropagation()}>
        {/* Modal Header */}
        <div className="modal-header">
          <div className="modal-title-wrap">
            <span className="finding-badge">Finding #{findingIndex + 1} of {totalFindings}</span>
            <h2 className="modal-title">
              {finding.algorithm} {finding.variant ? `(${finding.variant})` : ''}
            </h2>
          </div>

          <div className="modal-header-actions">
            {onNavigatePrev && (
              <button className="nav-icon-btn" onClick={onNavigatePrev} title="Previous finding">
                ←
              </button>
            )}
            {onNavigateNext && (
              <button className="nav-icon-btn" onClick={onNavigateNext} title="Next finding">
                →
              </button>
            )}
            <button className="modal-close-btn" onClick={onClose} title="Close details">
              ✕
            </button>
          </div>
        </div>

        {/* Modal Body */}
        <div className="modal-body">
          {/* Top Status Banner */}
          <div className="status-banner">
            <div className="status-banner-item">
              <span className="banner-label">Risk Level</span>
              <span className={`risk-badge large ${riskLevel.toLowerCase()}`}>
                {riskLevel} {riskAssessment ? `(${riskAssessment.riskScore}/100)` : ''}
              </span>
            </div>
            <div className="status-banner-item">
              <span className="banner-label">Quantum Risk</span>
              <span className={`quantum-badge large ${quantumRisk.toLowerCase()}`}>
                {quantumRisk}
              </span>
            </div>
            <div className="status-banner-item">
              <span className="banner-label">Category</span>
              <span className="purpose-tag large">{assetCategory.replace('_', ' ')}</span>
            </div>
            <div className="status-banner-item">
              <span className="banner-label">Lifecycle</span>
              <span className={`lifecycle-badge large ${lifecycleStatus.toLowerCase()}`}>
                {lifecycleStatus}
              </span>
            </div>
          </div>

          {/* Evidence Chain Visual Card */}
          <div className="evidence-chain-card">
            <div className="chain-title">Evidence Chain & Traceability</div>
            <div className="chain-flow">
              <div className="chain-node">
                <span className="node-step">1. Discovered Asset</span>
                <strong className="node-val">{finding.algorithm}</strong>
                <span className="node-sub">{assetCategory.replace('_', ' ')}</span>
              </div>
              <div className="chain-arrow">→</div>
              <div className="chain-node">
                <span className="node-step">2. Source File & Line</span>
                <strong className="node-val">{finding.file.split(/[\\/]/).pop()}</strong>
                <span className="node-sub font-mono">Line {finding.line}</span>
              </div>
              <div className="chain-arrow">→</div>
              <div className="chain-node">
                <span className="node-step">3. AST Evidence</span>
                <code className="node-code">{finding.evidence}</code>
              </div>
              <div className="chain-arrow">→</div>
              <div className="chain-node">
                <span className="node-step">4. Risk Assessment</span>
                <strong className={`node-val text-${riskLevel.toLowerCase()}`}>{riskLevel} ({riskAssessment?.riskScore || 0})</strong>
                <span className="node-sub">Quantum: {quantumRisk}</span>
              </div>
              {pqcRecommendation && pqcRecommendation.recommendedAlgorithm && (
                <>
                  <div className="chain-arrow">→</div>
                  <div className="chain-node">
                    <span className="node-step">5. PQC Target</span>
                    <strong className="node-val text-pqc">{pqcRecommendation.recommendedAlgorithm}</strong>
                    <span className="node-sub">Priority: {pqcRecommendation.migrationPriority}</span>
                  </div>
                </>
              )}
            </div>
          </div>

          <div className="details-grid">
            {/* Left Column: Asset & Code Evidence */}
            <div className="details-column">
              <div className="detail-section">
                <h3 className="section-heading">Cryptographic Asset Details</h3>
                <dl className="property-list">
                  <div className="prop-row">
                    <dt>Algorithm</dt>
                    <dd className="font-bold">{finding.algorithm}</dd>
                  </div>
                  <div className="prop-row">
                    <dt>Variant / Mode</dt>
                    <dd>{finding.variant || 'Not Specified'}</dd>
                  </div>
                  <div className="prop-row">
                    <dt>Declared Purpose</dt>
                    <dd>{finding.purpose}</dd>
                  </div>
                  <div className="prop-row">
                    <dt>Key Size</dt>
                    <dd>{finding.keySize ? `${finding.keySize} bits` : 'Not Statically Inferred'}</dd>
                  </div>
                  <div className="prop-row">
                    <dt>Source Type</dt>
                    <dd><code>{finding.sourceType}</code></dd>
                  </div>
                </dl>
              </div>

              {/* Phase 7 Enterprise Inventory Classification Section */}
              <div className="detail-section">
                <h3 className="section-heading">Enterprise Inventory & Classification</h3>
                <dl className="property-list">
                  <div className="prop-row">
                    <dt>Asset Category</dt>
                    <dd><span className="purpose-tag">{assetCategory.replace('_', ' ')}</span></dd>
                  </div>
                  <div className="prop-row">
                    <dt>Usage Mode</dt>
                    <dd>
                      <span className="badge-subtle">
                        {finding.usageCategory === 'DIRECT_USAGE' ? 'Direct API Invocation' :
                         finding.usageCategory === 'DEPENDENCY_PRESENCE' ? 'Dependency Presence' :
                         finding.usageCategory === 'INDIRECT_CONFIGURATION' ? 'Configuration / Protocol' : 'Unknown'}
                      </span>
                    </dd>
                  </div>
                  <div className="prop-row">
                    <dt>Lifecycle Status</dt>
                    <dd>
                      <span className={`lifecycle-badge ${lifecycleStatus.toLowerCase()}`}>
                        {lifecycleStatus}
                      </span>
                    </dd>
                  </div>
                  <div className="prop-row">
                    <dt>Business Criticality</dt>
                    <dd>
                      <span className="text-muted">
                        {finding.businessCriticality || 'UNKNOWN'} <small className="text-hint">(Static AST does not fabricate business context)</small>
                      </span>
                    </dd>
                  </div>
                  <div className="prop-row">
                    <dt>Data Sensitivity</dt>
                    <dd>
                      <span className="text-muted">
                        {finding.dataSensitivity || 'UNKNOWN'} <small className="text-hint">(Data classification requires runtime policy)</small>
                      </span>
                    </dd>
                  </div>
                  <div className="prop-row">
                    <dt>Cryptographic Library</dt>
                    <dd>{finding.library || 'Java Cryptography Architecture (JCA)'}</dd>
                  </div>
                  {finding.protocol && (
                    <div className="prop-row">
                      <dt>Protocol Version</dt>
                      <dd><span className="font-mono">{finding.protocol}</span></dd>
                    </div>
                  )}
                </dl>
              </div>

              <div className="detail-section">
                <h3 className="section-heading">Source Code Evidence</h3>
                <div className="code-evidence-box">
                  <div className="evidence-header">
                    <span className="evidence-file">{finding.file}</span>
                    <span className="evidence-line font-mono">Line: {finding.line}</span>
                  </div>
                  <pre className="evidence-code">
                    <code>{finding.evidence}</code>
                  </pre>
                </div>
              </div>

              {/* Risk Factors */}
              {riskAssessment && (
                <div className="detail-section">
                  <h3 className="section-heading">Risk Factors & Rationale</h3>
                  <div className="factors-list">
                    {riskAssessment.factors && riskAssessment.factors.length > 0 ? (
                      riskAssessment.factors.map((f, idx) => (
                        <div key={idx} className="factor-item">
                          <div className="factor-header">
                            <span className="factor-name">{f.name}</span>
                            <span className="factor-score">+{f.score} pts</span>
                          </div>
                          <p className="factor-explanation">{f.explanation}</p>
                        </div>
                      ))
                    ) : (
                      <p className="text-muted">No elevated risk factors detected for this primitive.</p>
                    )}
                  </div>
                </div>
              )}
            </div>

            {/* Right Column: PQC Migration Details */}
            <div className="details-column">
              {pqcRecommendation ? (
                <div className="detail-section highlight-pqc">
                  <h3 className="section-heading">Post-Quantum Cryptography (PQC) Migration</h3>
                  
                  <div className="pqc-status-box">
                    <div className="pqc-status-header">
                      <span>Status:</span>
                      <span className={`pqc-status-badge ${pqcRecommendation.recommendationStatus.toLowerCase()}`}>
                        {pqcRecommendation.recommendationStatus}
                      </span>
                    </div>
                    <div className="pqc-priority-header">
                      <span>Migration Priority:</span>
                      <span className={`priority-badge ${pqcRecommendation.migrationPriority.toLowerCase()}`}>
                        {pqcRecommendation.migrationPriority}
                      </span>
                    </div>
                  </div>

                  <dl className="property-list">
                    <div className="prop-row">
                      <dt>Recommended PQC</dt>
                      <dd>
                        {pqcRecommendation.recommendedAlgorithm ? (
                          <span className="algo-highlight">{pqcRecommendation.recommendedAlgorithm}</span>
                        ) : (
                          <span className="text-muted">None Required / Needs Analysis</span>
                        )}
                      </dd>
                    </div>

                    {pqcRecommendation.alternativeAlgorithms && pqcRecommendation.alternativeAlgorithms.length > 0 && (
                      <div className="prop-row">
                        <dt>Alternative Standards</dt>
                        <dd>
                          <div className="alt-algos">
                            {pqcRecommendation.alternativeAlgorithms.map((alt) => (
                              <span key={alt} className="badge-subtle">{alt}</span>
                            ))}
                          </div>
                        </dd>
                      </div>
                    )}
                  </dl>

                  {/* Rationale */}
                  <div className="pqc-block">
                    <span className="pqc-subheading">Cryptographic Rationale:</span>
                    <p className="pqc-text">{pqcRecommendation.rationale}</p>
                  </div>

                  {/* Migration Considerations */}
                  {pqcRecommendation.considerations && pqcRecommendation.considerations.length > 0 && (
                    <div className="pqc-block">
                      <span className="pqc-subheading">Engineering Considerations:</span>
                      <ul className="considerations-list">
                        {pqcRecommendation.considerations.map((c, idx) => (
                          <li key={idx}>{c}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              ) : (
                <div className="detail-section">
                  <h3 className="section-heading">PQC Guidance</h3>
                  <p className="text-muted">No specific PQC recommendation attached to this finding.</p>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="modal-footer">
          <span className="footer-tip">
            Press <kbd>Esc</kbd> or click backdrop to exit inspection.
          </span>
          <button className="btn-secondary" onClick={onClose}>
            Close
          </button>
        </div>
      </div>
    </div>
  );
};
