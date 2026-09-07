import React from 'react';
import type { AnalysisSummary } from '../types/analysis';

interface DashboardSummaryProps {
  summary: AnalysisSummary;
  sourcePath?: string;
  onNavigateTab: (tab: 'findings' | 'pqc' | 'cbom') => void;
}

export const DashboardSummary: React.FC<DashboardSummaryProps> = ({
  summary,
  sourcePath,
  onNavigateTab,
}) => {
  return (
    <div className="summary-section">
      <div className="section-header-row">
        <div>
          <h2 className="section-title">Cryptographic Posture & Inventory Summary</h2>
          {sourcePath && (
            <p className="section-subtitle">
              Analyzed Target: <code className="target-path">{sourcePath}</code>
            </p>
          )}
        </div>
      </div>

      <div className="kpi-grid">
        {/* Total Assets */}
        <div className="kpi-card highlight" onClick={() => onNavigateTab('findings')}>
          <div className="kpi-top">
            <span className="kpi-title">Discovered Assets</span>
            <div className="kpi-icon-wrap primary">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="2" y="2" width="20" height="8" rx="2" ry="2" />
                <rect x="2" y="14" width="20" height="8" rx="2" ry="2" />
                <line x1="6" y1="6" x2="6.01" y2="6" />
                <line x1="6" y1="18" x2="6.01" y2="18" />
              </svg>
            </div>
          </div>
          <div className="kpi-value">{summary.totalFindings}</div>
          <div className="kpi-footer">Total enterprise cryptographic primitives detected</div>
        </div>

        {/* Quantum High Risk */}
        <div className="kpi-card quantum" onClick={() => onNavigateTab('pqc')}>
          <div className="kpi-top">
            <span className="kpi-title">Quantum High Risk</span>
            <div className="kpi-icon-wrap quantum">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="3" />
                <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z" />
              </svg>
            </div>
          </div>
          <div className="kpi-value text-quantum">{summary.quantumHighRiskCount}</div>
          <div className="kpi-footer">Vulnerable to future quantum computing attacks</div>
        </div>

        {/* PQC Recommended */}
        <div className="kpi-card pqc" onClick={() => onNavigateTab('pqc')}>
          <div className="kpi-top">
            <span className="kpi-title">PQC Migration Required</span>
            <div className="kpi-icon-wrap pqc">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="16 3 21 3 21 8" />
                <line x1="4" y1="20" x2="21" y2="3" />
                <polyline points="21 16 21 21 16 21" />
                <line x1="15" y1="15" x2="21" y2="21" />
                <line x1="4" y1="4" x2="9" y2="9" />
              </svg>
            </div>
          </div>
          <div className="kpi-value text-pqc">{summary.pqcRecommendedCount}</div>
          <div className="kpi-footer">ML-DSA / ML-KEM candidates identified</div>
        </div>

        {/* Critical & High Risk */}
        <div className="kpi-card risk" onClick={() => onNavigateTab('findings')}>
          <div className="kpi-top">
            <span className="kpi-title">Elevated Classical Risk</span>
            <div className="kpi-icon-wrap critical">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
                <line x1="12" y1="9" x2="12" y2="13" />
                <line x1="12" y1="17" x2="12.01" y2="17" />
              </svg>
            </div>
          </div>
          <div className="kpi-value text-critical">
            {summary.criticalRiskCount + summary.highRiskCount}
          </div>
          <div className="kpi-footer">
            {summary.criticalRiskCount} Critical · {summary.highRiskCount} High
          </div>
        </div>
      </div>

      {/* Breakdown Mini Cards */}
      <div className="breakdown-grid">
        <div className="breakdown-card">
          <div className="breakdown-header">
            <span>Risk Level Distribution</span>
            <span className="breakdown-tag">Prototype Score (0-100)</span>
          </div>
          <div className="risk-pill-row">
            <div className="risk-count-item critical">
              <span className="count-num">{summary.criticalRiskCount}</span>
              <span className="count-label">Critical</span>
            </div>
            <div className="risk-count-item high">
              <span className="count-num">{summary.highRiskCount}</span>
              <span className="count-label">High</span>
            </div>
            <div className="risk-count-item medium">
              <span className="count-num">{summary.mediumRiskCount}</span>
              <span className="count-label">Medium</span>
            </div>
            <div className="risk-count-item low">
              <span className="count-num">{summary.lowRiskCount}</span>
              <span className="count-label">Low</span>
            </div>
          </div>
        </div>

        <div className="breakdown-card">
          <div className="breakdown-header">
            <span>PQC Recommendation Statuses</span>
            <span className="breakdown-tag">NIST Standard Mappings</span>
          </div>
          <div className="pqc-status-row">
            <div className="pqc-count-item recommended">
              <span className="count-num">{summary.pqcRecommendedCount}</span>
              <span className="count-label">Recommended</span>
            </div>
            <div className="pqc-count-item conditional">
              <span className="count-num">{summary.pqcConditionalCount}</span>
              <span className="count-label">Conditional</span>
            </div>
            <div className="pqc-count-item needs-analysis">
              <span className="count-num">{summary.pqcNeedsAnalysisCount}</span>
              <span className="count-label">Needs Analysis</span>
            </div>
            <div className="pqc-count-item not-required">
              <span className="count-num">{summary.pqcNotRequiredCount}</span>
              <span className="count-label">Not Required</span>
            </div>
          </div>
        </div>

        {/* Phase 7 Inventory Lifecycle Card */}
        {summary.activeAssetCount !== undefined && (
          <div className="breakdown-card">
            <div className="breakdown-header">
              <span>Inventory Lifecycle Status</span>
              <span className="breakdown-tag">Phase 7 Classification</span>
            </div>
            <div className="pqc-status-row">
              <div className="pqc-count-item recommended">
                <span className="count-num">{summary.activeAssetCount || 0}</span>
                <span className="count-label">Active</span>
              </div>
              <div className="pqc-count-item critical">
                <span className="count-num">{summary.deprecatedAssetCount || 0}</span>
                <span className="count-label">Deprecated</span>
              </div>
              <div className="pqc-count-item not-required">
                <span className="count-num">{summary.directUsageCount || 0}</span>
                <span className="count-label">Direct API</span>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
