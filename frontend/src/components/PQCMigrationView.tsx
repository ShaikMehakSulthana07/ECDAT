import React from 'react';
import type { CryptoFinding, RiskAssessment, PQCRecommendation } from '../types/analysis';

interface PQCMigrationViewProps {
  findings: CryptoFinding[];
  riskAssessments?: RiskAssessment[];
  pqcRecommendations: PQCRecommendation[];
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

export const PQCMigrationView: React.FC<PQCMigrationViewProps> = ({
  pqcRecommendations,
  onSelectFinding,
}) => {
  const total = pqcRecommendations.length;

  // Dimension 1: Mutually Exclusive PQC Target Standard Categories
  const mlDsaCount = pqcRecommendations.filter(
    (r) => r.recommendedAlgorithm?.includes('ML-DSA') || r.recommendedAlgorithm?.includes('SLH-DSA')
  ).length;
  const mlKemCount = pqcRecommendations.filter(
    (r) => r.recommendedAlgorithm?.includes('ML-KEM')
  ).length;
  const needsAnalysisTargetCount = Math.max(0, total - (mlDsaCount + mlKemCount));

  // Target Standard Percentages (Guaranteed exact 100% sum when total > 0)
  const mlDsaPct = total > 0 ? Math.round((mlDsaCount / total) * 100) : 0;
  const mlKemPct = total > 0 ? Math.round((mlKemCount / total) * 100) : 0;
  const needsAnalysisPct = total > 0 ? Math.max(0, 100 - (mlDsaPct + mlKemPct)) : 0;

  // Dimension 2: Migration Strategy Breakdown (Separated from target standard)
  const hybridCount = pqcRecommendations.filter((r) => r.migrationStrategy === 'HYBRID').length;
  const directPqcCount = pqcRecommendations.filter((r) => r.migrationStrategy === 'DIRECT_PQC').length;
  const needsAnalysisStratCount = pqcRecommendations.filter(
    (r) => r.migrationStrategy === 'NEEDS_ANALYSIS' || (!r.migrationStrategy && !r.recommendedAlgorithm)
  ).length;
  const noActionCount = pqcRecommendations.filter((r) => r.migrationStrategy === 'NO_ACTION').length;

  // Migration Priorities
  const highPriorityCount = pqcRecommendations.filter(
    (r) => r.migrationPriority === 'HIGH' || r.migrationPriority === 'CRITICAL'
  ).length;
  const medPriorityCount = pqcRecommendations.filter((r) => r.migrationPriority === 'MEDIUM').length;
  const lowPriorityCount = pqcRecommendations.filter((r) => r.migrationPriority === 'LOW').length;

  return (
    <div>
      {/* Header matching Screen 8 */}
      <div className="view-header">
        <div className="view-title-group">
          <h1 className="view-title">PQC Migration Strategy</h1>
          <p className="view-subtitle">Recommended post-quantum cryptography algorithms and migration paths</p>
        </div>
      </div>

      {/* Top Row: Target Standard Distribution & Migration Strategy Breakdown */}
      <div className="dashboard-charts-grid">
        {/* Left Card: Target Distribution Horizontal Bars (Mutually Exclusive Targets Only) */}
        <div className="chart-card">
          <div className="chart-card-header">
            <div>
              <h3 className="chart-card-title">Target Standard Distribution</h3>
              <span className="chart-card-sub">Post-quantum target mapping breakdown</span>
            </div>
            <span className="tag-subtle font-mono">{total} Total Recommendations</span>
          </div>

          <div className="pqc-standard-distribution-list">
            {/* ML-DSA (FIPS 204) */}
            <div className="pqc-dist-item">
              <div className="pqc-dist-header">
                <div className="pqc-dist-label-wrap">
                  <span className="legend-dot critical"></span>
                  <span className="pqc-dist-name">ML-DSA (FIPS 204)</span>
                  <span className="pqc-dist-tag">Digital Signatures</span>
                </div>
                <div className="pqc-dist-values font-mono">
                  <span className="pqc-dist-count">{mlDsaCount}</span>
                  <span className="pqc-dist-pct">{mlDsaPct}%</span>
                </div>
              </div>
              <div className="pqc-dist-track">
                <div
                  className="pqc-dist-fill dsa"
                  style={{ width: `${mlDsaPct}%` }}
                  title={`ML-DSA: ${mlDsaCount} (${mlDsaPct}%)`}
                ></div>
              </div>
            </div>

            {/* ML-KEM (FIPS 203) */}
            <div className="pqc-dist-item">
              <div className="pqc-dist-header">
                <div className="pqc-dist-label-wrap">
                  <span className="legend-dot high"></span>
                  <span className="pqc-dist-name">ML-KEM (FIPS 203)</span>
                  <span className="pqc-dist-tag">Key Encapsulation</span>
                </div>
                <div className="pqc-dist-values font-mono">
                  <span className="pqc-dist-count">{mlKemCount}</span>
                  <span className="pqc-dist-pct">{mlKemPct}%</span>
                </div>
              </div>
              <div className="pqc-dist-track">
                <div
                  className="pqc-dist-fill kem"
                  style={{ width: `${mlKemPct}%` }}
                  title={`ML-KEM: ${mlKemCount} (${mlKemPct}%)`}
                ></div>
              </div>
            </div>

            {/* Needs Analysis */}
            <div className="pqc-dist-item">
              <div className="pqc-dist-header">
                <div className="pqc-dist-label-wrap">
                  <span className="legend-dot medium"></span>
                  <span className="pqc-dist-name">Needs Analysis</span>
                  <span className="pqc-dist-tag">Context-Dependent</span>
                </div>
                <div className="pqc-dist-values font-mono">
                  <span className="pqc-dist-count">{needsAnalysisTargetCount}</span>
                  <span className="pqc-dist-pct">{needsAnalysisPct}%</span>
                </div>
              </div>
              <div className="pqc-dist-track">
                <div
                  className="pqc-dist-fill na"
                  style={{ width: `${needsAnalysisPct}%` }}
                  title={`Needs Analysis: ${needsAnalysisTargetCount} (${needsAnalysisPct}%)`}
                ></div>
              </div>
            </div>
          </div>
        </div>

        {/* Right Card: Migration Strategy & Urgency Breakdown */}
        <div className="chart-card">
          <div className="chart-card-header">
            <div>
              <h3 className="chart-card-title">Migration Strategy &amp; Urgency</h3>
              <span className="chart-card-sub">Transition approach and execution priority</span>
            </div>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', marginTop: '12px' }}>
            {/* Strategy Tiles */}
            <div>
              <div className="matrix-breakdown-title" style={{ marginBottom: '8px' }}>Migration Strategy Breakdown</div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px' }}>
                <div style={{ padding: '14px 10px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', textAlign: 'center' }}>
                  <span className="tag-subtle" style={{ color: 'var(--pqc-accent)', borderColor: 'var(--pqc-accent-border)' }}>Hybrid Transition</span>
                  <div className="font-bold font-mono" style={{ fontSize: '22px', color: 'var(--text-primary)', marginTop: '6px' }}>
                    {hybridCount}
                  </div>
                  <span style={{ fontSize: '10.5px', color: 'var(--text-muted)' }}>Dual / Composite Mode</span>
                </div>

                <div style={{ padding: '14px 10px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', textAlign: 'center' }}>
                  <span className="tag-subtle" style={{ color: 'var(--primary)', borderColor: 'var(--primary-border)' }}>Direct PQC</span>
                  <div className="font-bold font-mono" style={{ fontSize: '22px', color: 'var(--text-primary)', marginTop: '6px' }}>
                    {directPqcCount}
                  </div>
                  <span style={{ fontSize: '10.5px', color: 'var(--text-muted)' }}>Direct Standard Swap</span>
                </div>

                <div style={{ padding: '14px 10px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)', textAlign: 'center' }}>
                  <span className="tag-subtle" style={{ color: 'var(--text-secondary)' }}>Needs Analysis</span>
                  <div className="font-bold font-mono" style={{ fontSize: '22px', color: 'var(--text-primary)', marginTop: '6px' }}>
                    {needsAnalysisStratCount + noActionCount}
                  </div>
                  <span style={{ fontSize: '10.5px', color: 'var(--text-muted)' }}>Context / Retention</span>
                </div>
              </div>
            </div>

            {/* Migration Urgency Row */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 14px', background: 'var(--bg-surface-subtle)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-subtle)' }}>
              <span style={{ fontSize: '11.5px', fontWeight: 600, color: 'var(--text-secondary)' }}>Migration Urgency:</span>
              <div style={{ display: 'flex', gap: '8px' }}>
                <span className="risk-badge critical">{highPriorityCount} High</span>
                <span className="risk-badge medium">{medPriorityCount} Medium</span>
                <span className="risk-badge low">{lowPriorityCount} Low</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Bottom Table: Top Recommendations */}
      <div className="card-panel" style={{ marginTop: '20px' }}>
        <div className="card-panel-header">
          <div>
            <h3 className="card-panel-title">Top Recommendations</h3>
            <span className="card-panel-sub">Post-quantum cryptography mappings and transition strategies</span>
          </div>
        </div>

        <div className="table-container">
          <table className="enterprise-table pqc-recommendations-table">
            <thead>
              <tr>
                <th style={{ width: '18%' }}>Algorithm</th>
                <th style={{ width: '22%' }}>Current Use</th>
                <th style={{ width: '24%' }}>Recommended</th>
                <th style={{ width: '18%' }}>Strategy</th>
                <th style={{ width: '10%' }}>Priority</th>
                <th style={{ width: '8%', textAlign: 'right' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {pqcRecommendations.map((rec, idx) => {
                const rawStrategy = rec.migrationStrategy || (rec.recommendedAlgorithm ? 'DIRECT_PQC' : 'NEEDS_ANALYSIS');
                const strategyDisplay = formatTitleCase(rawStrategy);
                const purposeDisplay = formatTitleCase(rec.currentPurpose);

                return (
                  <tr
                    key={idx}
                    className="clickable-row pqc-row"
                    onClick={() => onSelectFinding(idx)}
                  >
                    <td>
                      <div className="algo-cell-block">
                        <span className="algo-primary-title">{rec.currentAlgorithm}</span>
                      </div>
                    </td>
                    <td>
                      <span className="table-purpose-text">{purposeDisplay}</span>
                    </td>
                    <td>
                      {rec.recommendedAlgorithm ? (
                        <div className="recommendation-focal-cell">
                          <span className="table-recommended-text">
                            {rec.recommendedAlgorithm}
                          </span>
                          <span className="table-recommended-sub">
                            {rec.recommendedAlgorithm.includes('ML-KEM')
                              ? 'FIPS 203'
                              : rec.recommendedAlgorithm.includes('ML-DSA')
                              ? 'FIPS 204'
                              : rec.recommendedAlgorithm.includes('SLH-DSA')
                              ? 'FIPS 205'
                              : 'NIST Standard'}
                          </span>
                        </div>
                      ) : (
                        <span className="table-text-muted">Needs Analysis</span>
                      )}
                    </td>
                    <td>
                      <span className="table-strategy-text">{strategyDisplay}</span>
                    </td>
                    <td>
                      <span className={`risk-badge ${rec.migrationPriority.toLowerCase()}`}>
                        {rec.migrationPriority}
                      </span>
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
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
