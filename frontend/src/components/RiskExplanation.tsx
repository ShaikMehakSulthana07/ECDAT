import React, { useState } from 'react';
import type { CryptoFinding, RiskAssessment, PQCRecommendation, CryptoAsset } from '../types/analysis';

export interface FindingDetailInput {
  finding?: CryptoFinding;
  asset?: CryptoAsset;
  riskAssessment?: RiskAssessment;
  pqcRecommendation?: PQCRecommendation;
}

export interface RiskExplanationProps {
  asset?: CryptoAsset;
  finding?: CryptoFinding;
  riskAssessment?: RiskAssessment;
  pqcRecommendation?: PQCRecommendation;
  compact?: boolean;
}

export const RiskExplanation: React.FC<RiskExplanationProps> = ({
  asset,
  finding,
  riskAssessment,
  pqcRecommendation,
  compact = false,
}) => {
  // Normalize finding & asset
  const effectiveFinding: CryptoFinding | undefined =
    finding ||
    asset?.originalFinding ||
    (asset
      ? {
          algorithm: asset.algorithm,
          variant: asset.variant,
          mode: asset.mode,
          padding: asset.padding,
          purpose: asset.purpose,
          keySize: asset.keySize,
          file: asset.sourceFile,
          line: asset.sourceLine,
          evidence: asset.evidence,
          confidence: asset.confidence,
          sourceType: asset.sourceType,
          assetCategory: asset.assetCategory,
          usageCategory: asset.usageCategory,
          lifecycleStatus: asset.lifecycleStatus,
          businessCriticality: asset.businessCriticality,
          dataSensitivity: asset.dataSensitivity,
          protocol: asset.protocol,
          library: asset.library,
        }
      : undefined);

  const effectiveRisk: RiskAssessment | undefined =
    riskAssessment || asset?.riskAssessment;

  const effectivePqc: PQCRecommendation | undefined =
    pqcRecommendation || asset?.pqcRecommendation;

  const quantumResult = effectiveRisk?.quantumRiskResult;

  const [expandedSection, setExpandedSection] = useState<string | null>(null);

  const toggleSection = (section: string) => {
    setExpandedSection((prev) => (prev === section ? null : section));
  };

  const formatKeySize = (keySize?: number | null) => {
    if (!keySize) return '';
    return `${keySize} bits`;
  };

  // Node 1: Cryptographic Artifact (Observed)
  const algorithm = effectiveFinding?.algorithm || asset?.algorithm || 'UNKNOWN';
  const variant = effectiveFinding?.variant || asset?.variant;
  const mode = effectiveFinding?.mode || asset?.mode;
  const padding = effectiveFinding?.padding || asset?.padding;
  const purpose = effectiveFinding?.purpose || asset?.purpose || 'UNKNOWN';
  const keySize = effectiveFinding?.keySize ?? asset?.keySize ?? null;

  // Node 2: Source Evidence (Observed)
  const sourceFile = effectiveFinding?.file || asset?.sourceFile || 'UNKNOWN';
  const sourceLine = effectiveFinding?.line ?? asset?.sourceLine ?? 0;
  const evidenceCode = effectiveFinding?.evidence || asset?.evidence || '';
  const confidence = effectiveFinding?.confidence || asset?.confidence || 'UNKNOWN';
  const sourceType = effectiveFinding?.sourceType || asset?.sourceType || '';

  // Node 3: Classical Security Assessment (Analytical Conclusion)
  const riskScore = effectiveRisk?.riskScore;
  const classicalRiskLevel = effectiveRisk?.riskLevel || 'UNKNOWN';
  const riskFactors = effectiveRisk?.factors || [];

  // Node 4: Quantum Assessment (Analytical Conclusion)
  let quantumStatus = 'UNKNOWN';
  if (quantumResult?.quantumVulnerabilityStatus) {
    quantumStatus = quantumResult.quantumVulnerabilityStatus;
  } else if (quantumResult?.quantumVulnerable !== undefined) {
    quantumStatus = quantumResult.quantumVulnerable ? 'VULNERABLE' : 'NOT_QUANTUM_VULNERABLE';
  } else if (effectiveRisk?.quantumRisk) {
    if (effectiveRisk.quantumRisk === 'HIGH') quantumStatus = 'VULNERABLE';
    else if (effectiveRisk.quantumRisk === 'LOW' || effectiveRisk.quantumRisk === 'NONE') quantumStatus = 'NOT_QUANTUM_VULNERABLE';
  }

  // Derive quantum reason strictly from backend quantumResult explanation/reasons
  const quantumExplanation = quantumResult?.explanation || '';
  // Fallback to explicit factor explanation if available in factors
  const quantumFactor = riskFactors.find((f) =>
    f.name.toLowerCase().includes('quantum') ||
    f.explanation.toLowerCase().includes('quantum') ||
    f.explanation.toLowerCase().includes('shor')
  );

  // Node 5: Mosca-style Timeline (Analytical Conclusion)
  const hasMosca =
    quantumResult &&
    (quantumResult.moscaConditionMet !== undefined ||
      quantumResult.migrationTimeYears !== undefined);
  const migrationTime = quantumResult?.migrationTimeYears;
  const dataLifetime = quantumResult?.dataLifetimeYears;
  const threatHorizon = quantumResult?.threatHorizonYears;
  const totalExposure = quantumResult?.totalExposureYears ?? (migrationTime != null && dataLifetime != null ? migrationTime + dataLifetime : undefined);
  const moscaConditionMet = quantumResult?.moscaConditionMet;
  const moscaCalculationDetails = quantumResult?.calculationDetails;

  // Node 6: Risk Classification (Analytical Conclusion)
  const overallRisk = effectiveRisk?.riskLevel || 'UNKNOWN';
  const riskReasons = effectiveRisk?.reasons || [];

  // Node 7: Migration Priority (Analytical Conclusion)
  let migrationPriority = 'UNKNOWN';
  if (effectivePqc?.migrationPriority) {
    migrationPriority = effectivePqc.migrationPriority;
  } else if (quantumResult?.migrationUrgency && quantumResult.migrationUrgency !== 'UNKNOWN') {
    migrationPriority = quantumResult.migrationUrgency;
  }

  // Node 8: PQC / Hybrid Recommendation (Analytical Conclusion)
  const pqcStatus = effectivePqc?.recommendationStatus || 'UNKNOWN';
  const recommendedAlgo = effectivePqc?.recommendedAlgorithm || null;
  const alternativeAlgos = effectivePqc?.alternativeAlgorithms || [];
  const pqcRationale = effectivePqc?.rationale || '';
  const pqcStrategy = effectivePqc?.migrationStrategy;
  const pqcConsiderations = effectivePqc?.considerations || [];

  return (
    <div className={`evidence-chain-container ${compact ? 'compact' : ''}`}>
      {/* 10-Second Executive Summary Hero */}
      <div className="evidence-chain-hero">
        <div className="hero-verdict-left">
          <span className="hero-eyebrow">10-SECOND VERDICT &amp; REASONING PATH</span>
          <div className="hero-headline">
            <span className="hero-algo">{algorithm}</span>
            {keySize && <span className="hero-algo-sub">-{keySize}</span>}
            <span className="hero-arrow">→</span>
            <span
              className={`hero-quantum-badge ${
                quantumStatus === 'VULNERABLE'
                  ? 'vuln'
                  : quantumStatus === 'NOT_QUANTUM_VULNERABLE'
                  ? 'safe'
                  : 'neutral'
              }`}
            >
              {quantumStatus === 'VULNERABLE'
                ? 'QUANTUM VULNERABLE'
                : quantumStatus === 'NOT_QUANTUM_VULNERABLE'
                ? 'QUANTUM SAFE / RESISTANT'
                : 'QUANTUM UNKNOWN'}
            </span>
            <span className="hero-arrow">→</span>
            <span className={`hero-risk-badge ${overallRisk.toLowerCase()}`}>
              {overallRisk} RISK
            </span>
          </div>
          <div className="hero-recommendation-sub">
            Target: <strong>{recommendedAlgo || 'Evaluate NIST PQC / Hybrid Strategy'}</strong>
            {migrationPriority !== 'UNKNOWN' && (
              <span> &bull; Urgency: <strong>{migrationPriority}</strong></span>
            )}
          </div>
        </div>

        <div className="hero-legend">
          <span className="legend-tag observed">
            <span className="legend-dot obs"></span> OBSERVED EVIDENCE
          </span>
          <span className="legend-tag analytical">
            <span className="legend-dot ana"></span> ANALYTICAL CONCLUSION
          </span>
        </div>
      </div>

      {/* Structured Evidence Chain Steps */}
      <div className="evidence-chain-nodes">
        {/* Step 1: Cryptographic Artifact */}
        <div className="chain-card observed" data-testid="step-cryptographic-artifact">
          <div className="chain-step-header">
            <div className="step-badge-group">
              <span className="step-number">1</span>
              <span className="step-title">Cryptographic Artifact</span>
            </div>
            <span className="provenance-pill observed">OBSERVED EVIDENCE</span>
          </div>
          <div className="chain-card-body">
            <div className="chain-artifact-main">
              <span className="artifact-name font-mono">
                {algorithm}
                {keySize ? `-${keySize}` : ''}
                {variant && variant !== algorithm ? ` (${variant})` : ''}
              </span>
              {mode && <span className="artifact-mode font-mono">/ {mode}</span>}
              {padding && <span className="artifact-padding font-mono">/ {padding}</span>}
            </div>
            <div className="chain-meta-row">
              <span className="meta-label">Purpose:</span>
              <span className="meta-value tag-subtle">{purpose.replace(/_/g, ' ')}</span>
              {keySize && (
                <>
                  <span className="meta-label">Key Size:</span>
                  <span className="meta-value font-mono">{formatKeySize(keySize)}</span>
                </>
              )}
            </div>
          </div>
        </div>

        <div className="chain-connector">↓</div>

        {/* Step 2: Source Evidence */}
        <div className="chain-card observed" data-testid="step-source-evidence">
          <div className="chain-step-header">
            <div className="step-badge-group">
              <span className="step-number">2</span>
              <span className="step-title">Source Evidence</span>
            </div>
            <span className="provenance-pill observed">OBSERVED EVIDENCE</span>
          </div>
          <div className="chain-card-body">
            <div className="source-ref-line font-mono">
              <span className="file-name">{sourceFile}</span>
              <span className="line-num">:{sourceLine}</span>
              {sourceType && <span className="source-type-tag">via {sourceType}</span>}
            </div>
            {evidenceCode && (
              <div className="code-evidence-box">
                <code>{evidenceCode}</code>
              </div>
            )}
            <div className="chain-meta-row">
              <span className="meta-label">Observation Confidence:</span>
              <span className={`confidence-tag ${confidence.toLowerCase()}`}>
                {confidence}
              </span>
            </div>
          </div>
        </div>

        <div className="chain-connector">↓</div>

        {/* Step 3: Classical Security Assessment */}
        <div className="chain-card analytical" data-testid="step-classical-security">
          <div className="chain-step-header">
            <div className="step-badge-group">
              <span className="step-number">3</span>
              <span className="step-title">Classical Security Assessment</span>
            </div>
            <span className="provenance-pill analytical">ANALYTICAL CONCLUSION</span>
          </div>
          <div className="chain-card-body">
            <div className="assessment-score-row">
              <div className="metric-capsule">
                <span className="capsule-label">Classical Risk Level</span>
                <span className={`capsule-val risk-badge ${classicalRiskLevel.toLowerCase()}`}>
                  {classicalRiskLevel}
                </span>
              </div>
              {riskScore !== undefined && (
                <div className="metric-capsule">
                  <span className="capsule-label">Risk Score</span>
                  <span className="capsule-val font-mono">{riskScore} / 100</span>
                </div>
              )}
            </div>

            {riskFactors.length > 0 && (
              <div className="risk-factors-list">
                <div className="factors-heading">Contributing Classical Risk Factors:</div>
                {riskFactors.map((factor, idx) => (
                  <div key={idx} className="factor-pill">
                    <span className="factor-name font-mono">{factor.name}</span>
                    <span className="factor-score font-mono">+{factor.score}</span>
                    <span className="factor-desc">{factor.explanation}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="chain-connector">↓</div>

        {/* Step 4: Quantum Assessment */}
        <div className="chain-card analytical" data-testid="step-quantum-assessment">
          <div className="chain-step-header">
            <div className="step-badge-group">
              <span className="step-number">4</span>
              <span className="step-title">Quantum Assessment</span>
            </div>
            <span className="provenance-pill analytical">ANALYTICAL CONCLUSION</span>
          </div>
          <div className="chain-card-body">
            <div className="quantum-status-row">
              <span
                className={`quantum-status-pill ${
                  quantumStatus === 'VULNERABLE'
                    ? 'vuln'
                    : quantumStatus === 'NOT_QUANTUM_VULNERABLE'
                    ? 'safe'
                    : 'unknown'
                }`}
              >
                {quantumStatus === 'VULNERABLE'
                  ? 'VULNERABLE'
                  : quantumStatus === 'NOT_QUANTUM_VULNERABLE'
                  ? 'NOT QUANTUM VULNERABLE'
                  : 'UNKNOWN'}
              </span>
            </div>

            {/* Claim strictly from actual backend data */}
            {quantumExplanation ? (
              <div className="quantum-reason-box">
                <span className="reason-title">Backend Quantum Assessment:</span>
                <p className="reason-text font-mono">{quantumExplanation}</p>
              </div>
            ) : quantumFactor ? (
              <div className="quantum-reason-box">
                <span className="reason-title">Reason:</span>
                <p className="reason-text">{quantumFactor.explanation}</p>
              </div>
            ) : (
              <div className="quantum-reason-box muted">
                <p className="reason-text text-muted">
                  {quantumStatus === 'UNKNOWN'
                    ? 'Quantum vulnerability status is UNKNOWN: No explicit rule configured for this primitive.'
                    : `Evaluated as ${quantumStatus} under current cryptographic threat model.`}
                </p>
              </div>
            )}
          </div>
        </div>

        <div className="chain-connector">↓</div>

        {/* Step 5: Mosca-style Timeline */}
        <div className="chain-card analytical" data-testid="step-mosca-timeline">
          <div className="chain-step-header">
            <div className="step-badge-group">
              <span className="step-number">5</span>
              <span className="step-title">Mosca-style Timeline</span>
            </div>
            <span className="provenance-pill analytical">ANALYTICAL CONCLUSION</span>
          </div>
          <div className="chain-card-body">
            {hasMosca ? (
              <>
                <div className="mosca-calc-grid">
                  <div className="mosca-var-box">
                    <span className="var-sym">X</span>
                    <span className="var-lbl">Migration Time</span>
                    <span className="var-val font-mono">{migrationTime ?? 'UNKNOWN'} yrs</span>
                  </div>
                  <div className="mosca-operator">+</div>
                  <div className="mosca-var-box">
                    <span className="var-sym">Y</span>
                    <span className="var-lbl">Data Lifetime</span>
                    <span className="var-val font-mono">{dataLifetime ?? 'UNKNOWN'} yrs</span>
                  </div>
                  <div className="mosca-operator">&gt;</div>
                  <div className="mosca-var-box">
                    <span className="var-sym">Z</span>
                    <span className="var-lbl">Threat Horizon</span>
                    <span className="var-val font-mono">{threatHorizon ?? 'UNKNOWN'} yrs</span>
                  </div>
                </div>

                <div
                  className={`mosca-verdict-box ${
                    moscaConditionMet === true
                      ? 'exposed'
                      : moscaConditionMet === false
                      ? 'safe'
                      : 'unknown'
                  }`}
                >
                  <div className="mosca-verdict-title">
                    {moscaConditionMet === true
                      ? 'EXPOSED: Total Exposure (X + Y) > Threat Horizon (Z)'
                      : moscaConditionMet === false
                      ? 'NOT EXPOSED: Total Exposure (X + Y) ≤ Threat Horizon (Z)'
                      : 'TIMELINE ASSESSMENT: UNKNOWN'}
                  </div>
                  <div className="mosca-verdict-desc">
                    {totalExposure !== undefined && threatHorizon !== undefined ? (
                      <span>
                        Total operational exposure is <strong>{totalExposure} years</strong> versus a configured
                        quantum threat horizon of <strong>{threatHorizon} years</strong>.
                      </span>
                    ) : (
                      <span>Timeline parameters have not been fully configured for this target.</span>
                    )}
                  </div>
                </div>

                {moscaCalculationDetails && (
                  <div className="mosca-details-toggle">
                    <button
                      className="btn-link-xs"
                      onClick={() => toggleSection('mosca')}
                      type="button"
                    >
                      {expandedSection === 'mosca' ? '▲ Hide Calculation Formula' : '▼ View Calculation Details'}
                    </button>
                    {expandedSection === 'mosca' && (
                      <pre className="mosca-raw-details font-mono">
                        {moscaCalculationDetails}
                      </pre>
                    )}
                  </div>
                )}
              </>
            ) : (
              <div className="no-data-msg">
                <span className="text-muted">
                  Mosca timeline analysis: <strong>UNKNOWN</strong> (project-level migration parameters not supplied).
                </span>
              </div>
            )}
          </div>
        </div>

        <div className="chain-connector">↓</div>

        {/* Step 6: Risk Classification */}
        <div className="chain-card analytical" data-testid="step-risk-classification">
          <div className="chain-step-header">
            <div className="step-badge-group">
              <span className="step-number">6</span>
              <span className="step-title">Risk Classification</span>
            </div>
            <span className="provenance-pill analytical">ANALYTICAL CONCLUSION</span>
          </div>
          <div className="chain-card-body">
            <div className="risk-level-headline">
              <span className="risk-lead">Overall Risk Rating:</span>
              <span className={`risk-badge large ${overallRisk.toLowerCase()}`}>
                {overallRisk}
              </span>
              {riskScore !== undefined && (
                <span className="risk-score-callout font-mono">
                  Score: {riskScore}/100
                </span>
              )}
            </div>

            {riskReasons.length > 0 ? (
              <ul className="risk-reasons-list">
                {riskReasons.map((reason, idx) => (
                  <li key={idx} className="reason-item">
                    {reason}
                  </li>
                ))}
              </ul>
            ) : (
              <div className="text-muted" style={{ fontSize: '12px', marginTop: '6px' }}>
                Classification generated from engine rules and configured business sensitivity.
              </div>
            )}
          </div>
        </div>

        <div className="chain-connector">↓</div>

        {/* Step 7: Migration Priority */}
        <div className="chain-card analytical" data-testid="step-migration-priority">
          <div className="chain-step-header">
            <div className="step-badge-group">
              <span className="step-number">7</span>
              <span className="step-title">Migration Priority</span>
            </div>
            <span className="provenance-pill analytical">ANALYTICAL CONCLUSION</span>
          </div>
          <div className="chain-card-body">
            <div className="priority-row">
              <span className="priority-label">Urgency &amp; Schedule:</span>
              <span className={`risk-badge ${migrationPriority.toLowerCase()}`}>
                {migrationPriority} PRIORITY
              </span>
            </div>
            <div className="priority-note">
              {migrationPriority === 'CRITICAL' &&
                'Immediate migration planning required. Store Now, Decrypt Later (SNDL) exposure window active.'}
              {migrationPriority === 'HIGH' &&
                'High-priority modernization candidate. Include in near-term post-quantum transition wave.'}
              {migrationPriority === 'MEDIUM' &&
                'Medium-term transition candidate. Monitor standard finalization and library support.'}
              {migrationPriority === 'LOW' &&
                'Low immediate quantum risk or timeline fits safely within configured threat horizon.'}
              {migrationPriority === 'UNKNOWN' &&
                'Migration priority is UNKNOWN: requires further context or threat horizon specification.'}
            </div>
          </div>
        </div>

        <div className="chain-connector">↓</div>

        {/* Step 8: PQC / Hybrid Recommendation */}
        <div className="chain-card analytical" data-testid="step-pqc-recommendation">
          <div className="chain-step-header">
            <div className="step-badge-group">
              <span className="step-number">8</span>
              <span className="step-title">PQC / Hybrid Recommendation</span>
            </div>
            <span className="provenance-pill analytical">ANALYTICAL CONCLUSION</span>
          </div>
          <div className="chain-card-body">
            <div className="pqc-recommendation-hero">
              <div className="pqc-status-badge-wrap">
                <span className="meta-label">Status:</span>
                <span
                  className={`recommendation-status-pill ${pqcStatus.toLowerCase()}`}
                >
                  {pqcStatus}
                </span>
              </div>
              {pqcStrategy && (
                <div className="pqc-strategy-wrap">
                  <span className="meta-label">Strategy:</span>
                  <span className="tag-subtle font-mono">{pqcStrategy}</span>
                </div>
              )}
            </div>

            {recommendedAlgo ? (
              <div className="pqc-algo-selection">
                <div className="target-algo-banner">
                  <span className="target-lbl">Recommended Target:</span>
                  <span className="target-name font-mono">{recommendedAlgo}</span>
                </div>
                {alternativeAlgos.length > 0 && (
                  <div className="alternative-algos-row">
                    <span className="meta-label">Alternatives:</span>
                    <span className="font-mono">{alternativeAlgos.join(', ')}</span>
                  </div>
                )}
              </div>
            ) : (
              <div className="pqc-algo-selection not-required">
                <span className="meta-label">Target:</span>
                <span className="font-mono">
                  {pqcStatus === 'NOT_REQUIRED'
                    ? 'No PQC replacement required for this primitive'
                    : 'NEEDS ANALYSIS / UNKNOWN'}
                </span>
              </div>
            )}

            {pqcRationale && (
              <div className="pqc-rationale-box">
                <span className="rationale-lbl">Cryptographic Rationale:</span>
                <p className="rationale-text">{pqcRationale}</p>
              </div>
            )}

            {pqcConsiderations.length > 0 && (
              <div className="pqc-considerations-box">
                <button
                  className="btn-link-xs"
                  onClick={() => toggleSection('considerations')}
                  type="button"
                >
                  {expandedSection === 'considerations'
                    ? '▲ Hide Implementation Considerations'
                    : `▼ View Implementation Considerations (${pqcConsiderations.length})`}
                </button>
                {expandedSection === 'considerations' && (
                  <ul className="considerations-list">
                    {pqcConsiderations.map((item, idx) => (
                      <li key={idx}>{item}</li>
                    ))}
                  </ul>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default RiskExplanation;
