import React from 'react';
import type { CryptoFinding, RiskAssessment, PQCRecommendation } from '../types/analysis';

interface SourceTraceabilityProps {
  findings: CryptoFinding[];
  riskAssessments: RiskAssessment[];
  pqcRecommendations: PQCRecommendation[];
  onSelectFinding: (index: number) => void;
}

export const SourceTraceability: React.FC<SourceTraceabilityProps> = ({
  findings,
  riskAssessments,
  pqcRecommendations,
  onSelectFinding,
}) => {
  return (
    <div className="traceability-view">
      <div className="card-panel">
        <div className="panel-header">
          <div className="panel-title-group">
            <h3 className="panel-title">End-to-End Cryptographic Traceability</h3>
            <span className="panel-sub">
              Deterministic evidence audit chain: AST Finding ➔ Source Location ➔ Code Evidence ➔ Risk Evaluation ➔ PQC Migration
            </span>
          </div>
        </div>

        <div className="traceability-list">
          {findings.map((finding, idx) => {
            const risk = riskAssessments[idx];
            const pqc = pqcRecommendations[idx];
            const riskLevel = risk?.riskLevel || 'LOW';
            const quantumRisk = risk?.quantumRisk || 'NONE';

            return (
              <div
                key={idx}
                className="traceability-card"
                onClick={() => onSelectFinding(idx)}
              >
                <div className="traceability-header-row">
                  <div className="trace-title">
                    <span className="trace-index">#{idx + 1}</span>
                    <strong className="algo-name">{finding.algorithm}</strong>
                    {finding.variant && <span className="badge-subtle">{finding.variant}</span>}
                    <span className="purpose-tag">{finding.purpose}</span>
                  </div>
                  <div className="trace-badges">
                    <span className={`risk-badge ${riskLevel.toLowerCase()}`}>
                      {riskLevel} ({risk?.riskScore || 0})
                    </span>
                    <span className={`quantum-badge ${quantumRisk.toLowerCase()}`}>
                      {quantumRisk}
                    </span>
                    {pqc?.recommendedAlgorithm && (
                      <span className="pqc-target-badge">
                        Target: {pqc.recommendedAlgorithm}
                      </span>
                    )}
                  </div>
                </div>

                <div className="trace-chain-visual">
                  {/* Step 1: Discovered Asset */}
                  <div className="trace-step-box">
                    <div className="step-label">1. Discovered Asset</div>
                    <div className="step-content">
                      <strong>{finding.algorithm}</strong>
                      <span className="text-muted">{finding.purpose}</span>
                      {finding.keySize && <span className="font-mono">{finding.keySize} bits</span>}
                    </div>
                  </div>

                  <div className="step-connector">➔</div>

                  {/* Step 2: Source File Location */}
                  <div className="step-box">
                    <div className="step-label">2. Source File & Line</div>
                    <div className="step-content">
                      <span className="font-mono text-file">{finding.file.split(/[\\/]/).pop()}</span>
                      <span className="font-mono text-line">Line {finding.line}</span>
                    </div>
                  </div>

                  <div className="step-connector">➔</div>

                  {/* Step 3: AST Code Evidence */}
                  <div className="step-box evidence-box">
                    <div className="step-label">3. AST Code Evidence</div>
                    <div className="step-content">
                      <code className="evidence-code-inline">{finding.evidence}</code>
                      <span className={`confidence-badge ${finding.confidence.toLowerCase()}`}>
                        {finding.confidence} Confidence
                      </span>
                    </div>
                  </div>

                  <div className="step-connector">➔</div>

                  {/* Step 4: Risk Assessment */}
                  <div className="step-box">
                    <div className="step-label">4. Risk Assessment</div>
                    <div className="step-content">
                      <strong className={`text-${riskLevel.toLowerCase()}`}>
                        {riskLevel} ({risk?.riskScore || 0}/100)
                      </strong>
                      <span className="text-muted">{risk?.factors?.length || 0} Factors</span>
                    </div>
                  </div>

                  <div className="step-connector">➔</div>

                  {/* Step 5: PQC Migration */}
                  <div className="step-box">
                    <div className="step-label">5. PQC Migration</div>
                    <div className="step-content">
                      {pqc?.recommendedAlgorithm ? (
                        <strong className="text-pqc">{pqc.recommendedAlgorithm}</strong>
                      ) : (
                        <span className="text-muted">{pqc?.recommendationStatus || 'None'}</span>
                      )}
                      {pqc?.migrationPriority && (
                        <span className={`priority-badge ${pqc.migrationPriority.toLowerCase()}`}>
                          {pqc.migrationPriority}
                        </span>
                      )}
                    </div>
                  </div>
                </div>

                {risk?.reasons && risk.reasons.length > 0 && (
                  <div className="trace-reasons">
                    <span className="reason-label">Key Risk Rationale:</span>
                    <p className="reason-text">{risk.reasons[0]}</p>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
