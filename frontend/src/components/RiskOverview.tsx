import React from 'react';
import type { RiskAssessment, AnalysisSummary } from '../types/analysis';

interface RiskOverviewProps {
  riskAssessments: RiskAssessment[];
  summary: AnalysisSummary;
}

export const RiskOverview: React.FC<RiskOverviewProps> = ({
  riskAssessments,
  summary,
}) => {
  const total = summary.totalFindings || 1;
  const criticalPct = Math.round((summary.criticalRiskCount / total) * 100);
  const highPct = Math.round((summary.highRiskCount / total) * 100);
  const mediumPct = Math.round((summary.mediumRiskCount / total) * 100);
  const lowPct = Math.round((summary.lowRiskCount / total) * 100);

  // Group assessments by algorithm for overview table
  const algorithmSummary = riskAssessments.reduce((acc, curr) => {
    const algo = curr.originalFinding.algorithm;
    if (!acc[algo]) {
      acc[algo] = {
        count: 0,
        maxScore: 0,
        highestRiskLevel: curr.riskLevel,
        quantumRisk: curr.quantumRisk,
        purposes: new Set<string>(),
      };
    }
    acc[algo].count += 1;
    acc[algo].maxScore = Math.max(acc[algo].maxScore, curr.riskScore);
    acc[algo].purposes.add(curr.originalFinding.purpose);
    return acc;
  }, {} as Record<string, { count: number; maxScore: number; highestRiskLevel: string; quantumRisk: string; purposes: Set<string> }>);

  return (
    <div className="risk-overview-container">
      <div className="card-panel">
        <div className="panel-header">
          <div className="panel-title-group">
            <h3 className="panel-title">Risk Distribution Bar</h3>
            <span className="panel-sub">Proportional breakdown of discovered cryptographic assets</span>
          </div>
          <span className="disclaimer-badge">
            Prototype Risk Methodology (0–100)
          </span>
        </div>

        {/* Stacked Proportional Bar */}
        <div className="stacked-bar-container">
          <div className="stacked-bar">
            {summary.criticalRiskCount > 0 && (
              <div
                className="bar-segment critical"
                style={{ width: `${criticalPct}%` }}
                title={`Critical Risk: ${summary.criticalRiskCount} (${criticalPct}%)`}
              >
                {criticalPct >= 10 && `${criticalPct}%`}
              </div>
            )}
            {summary.highRiskCount > 0 && (
              <div
                className="bar-segment high"
                style={{ width: `${highPct}%` }}
                title={`High Risk: ${summary.highRiskCount} (${highPct}%)`}
              >
                {highPct >= 10 && `${highPct}%`}
              </div>
            )}
            {summary.mediumRiskCount > 0 && (
              <div
                className="bar-segment medium"
                style={{ width: `${mediumPct}%` }}
                title={`Medium Risk: ${summary.mediumRiskCount} (${mediumPct}%)`}
              >
                {mediumPct >= 10 && `${mediumPct}%`}
              </div>
            )}
            {summary.lowRiskCount > 0 && (
              <div
                className="bar-segment low"
                style={{ width: `${lowPct}%` }}
                title={`Low Risk: ${summary.lowRiskCount} (${lowPct}%)`}
              >
                {lowPct >= 10 && `${lowPct}%`}
              </div>
            )}
          </div>

          <div className="bar-legend">
            <div className="legend-item">
              <span className="legend-dot critical"></span>
              <span>Critical (75–100): <strong>{summary.criticalRiskCount}</strong></span>
            </div>
            <div className="legend-item">
              <span className="legend-dot high"></span>
              <span>High (50–74): <strong>{summary.highRiskCount}</strong></span>
            </div>
            <div className="legend-item">
              <span className="legend-dot medium"></span>
              <span>Medium (25–49): <strong>{summary.mediumRiskCount}</strong></span>
            </div>
            <div className="legend-item">
              <span className="legend-dot low"></span>
              <span>Low (0–24): <strong>{summary.lowRiskCount}</strong></span>
            </div>
          </div>
        </div>

        {/* Methodology Notice */}
        <div className="methodology-note">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="16" x2="12" y2="12" />
            <line x1="12" y1="8" x2="12.01" y2="8" />
          </svg>
          <span>
            ECDAT evaluates risk using deterministic rules assessing key length, purpose, classical vulnerability (e.g. SHA-1/MD5 collision weaknesses), and quantum exposure. Risk scores reflect prototype methodology for prioritization.
          </span>
        </div>
      </div>

      {/* Algorithm Posture Breakdown */}
      <div className="card-panel">
        <div className="panel-header">
          <div className="panel-title-group">
            <h3 className="panel-title">Algorithm Posture Analysis</h3>
            <span className="panel-sub">Aggregated view by cryptographic primitive</span>
          </div>
        </div>

        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th>Algorithm</th>
                <th>Purposes</th>
                <th>Instances</th>
                <th>Peak Risk Score</th>
                <th>Risk Tier</th>
                <th>Quantum Vulnerability</th>
              </tr>
            </thead>
            <tbody>
              {Object.entries(algorithmSummary).map(([algo, data]) => (
                <tr key={algo}>
                  <td>
                    <span className="algo-badge">{algo}</span>
                  </td>
                  <td>
                    <span className="purpose-tags">
                      {Array.from(data.purposes).map((p) => (
                        <span key={p} className="badge-subtle">{p}</span>
                      ))}
                    </span>
                  </td>
                  <td>
                    <span className="font-mono">{data.count}</span>
                  </td>
                  <td>
                    <div className="score-meter">
                      <div
                        className={`score-meter-fill ${data.highestRiskLevel.toLowerCase()}`}
                        style={{ width: `${data.maxScore}%` }}
                      ></div>
                      <span className="score-number font-mono">{data.maxScore}</span>
                    </div>
                  </td>
                  <td>
                    <span className={`risk-badge ${data.highestRiskLevel.toLowerCase()}`}>
                      {data.highestRiskLevel}
                    </span>
                  </td>
                  <td>
                    <span className={`quantum-badge ${data.quantumRisk.toLowerCase()}`}>
                      {data.quantumRisk}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
