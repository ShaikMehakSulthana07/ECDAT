import React, { useState, useMemo } from 'react';
import type { AnalysisResponse } from '../types/analysis';

interface DashboardViewProps {
  analysisData: AnalysisResponse | null;
  onNavigate: (page: 'scan' | 'inventory' | 'certificates' | 'dependencies' | 'quantum' | 'pqc' | 'cbom' | 'reports') => void;
  onSelectFinding: (index: number) => void;
  onQuickScan: () => void;
  isLoading: boolean;
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

export const DashboardView: React.FC<DashboardViewProps> = ({
  analysisData,
  onNavigate,
  onSelectFinding,
  onQuickScan,
  isLoading,
}) => {
  const [hoveredDonutSegment, setHoveredDonutSegment] = useState<string | null>(null);

  // If no analysis data is loaded, render the clean enterprise empty state
  if (!analysisData) {
    return (
      <div className="dashboard-empty-container">
        <div className="empty-state-card enterprise-empty">
          <div className="empty-state-icon-wrapper">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.75">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              <circle cx="12" cy="11" r="3" />
              <path d="m9 17 3-2 3 2" />
            </svg>
          </div>
          <h2 className="empty-state-title">No Analysis Available</h2>
          <p className="empty-state-desc">
            ECDAT performs automated static analysis across source code, configuration files, and key stores to discover cryptographic primitives, evaluate quantum risk under Shor&apos;s algorithm, and generate post-quantum migration plans.
          </p>
          <div className="empty-actions-row">
            <button className="btn-primary" onClick={() => onNavigate('scan')}>
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="17 8 12 3 7 8" />
                <line x1="12" y1="3" x2="12" y2="9" />
              </svg>
              Scan Project Archive (.zip)
            </button>
            <button className="btn-secondary" onClick={onQuickScan} disabled={isLoading}>
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polygon points="5 3 19 12 5 21 5 3" />
              </svg>
              {isLoading ? 'Analyzing Target...' : 'Scan Default Target'}
            </button>
          </div>
        </div>
      </div>
    );
  }

  const { summary, findings, riskAssessments, pqcRecommendations, context } = analysisData;

  // Key metrics calculation
  const totalAssets = summary.totalFindings || findings.length || 0;
  const criticalCount = summary.criticalRiskCount || 0;
  const highCount = summary.highRiskCount || 0;
  const mediumCount = summary.mediumRiskCount || 0;
  const lowCount = summary.lowRiskCount || 0;
  const highCriticalCount = criticalCount + highCount;

  // Quantum calculations
  const quantumVulnCount = summary.quantumHighRiskCount ||
    findings.filter((_, idx) => {
      const r = riskAssessments[idx];
      return r?.quantumRisk === 'HIGH' || r?.quantumRiskResult?.quantumVulnerable;
    }).length;

  const quantumSafeCount = Math.max(0, totalAssets - quantumVulnCount);
  const migrationRequiredCount = summary.pqcRecommendedCount ||
    pqcRecommendations.filter((p) => p.recommendationStatus === 'RECOMMENDED').length;
  const pqcRecCount = pqcRecommendations.length || 0;

  // Mosca Theorem Parameters
  const migrationTime = context?.migrationTimeYears ?? 3;
  const dataLifetime = context?.dataLifetimeYears ?? 10;
  const threatHorizon = context?.threatHorizonYears ?? 10;
  const totalExposure = migrationTime + dataLifetime;
  const moscaConditionMet = totalExposure > threatHorizon;

  // Percentages for Donut chart
  const denom = totalAssets > 0 ? totalAssets : 1;
  const critPct = Math.round((criticalCount / denom) * 100);
  const highPct = Math.round((highCount / denom) * 100);
  const medPct = Math.round((mediumCount / denom) * 100);
  const lowPct = Math.max(0, 100 - (critPct + highPct + medPct));

  // Compute SVG Donut segments
  const radius = 38;
  const circumference = 2 * Math.PI * radius; // ~238.76

  const critDash = (critPct / 100) * circumference;
  const highDash = (highPct / 100) * circumference;
  const medDash = (medPct / 100) * circumference;
  const lowDash = (lowPct / 100) * circumference;

  const critOffset = 0;
  const highOffset = -critDash;
  const medOffset = -(critDash + highDash);
  const lowOffset = -(critDash + highDash + medDash);

  // Quantum Exposure ratio
  const quantumVulnPct = Math.round((quantumVulnCount / denom) * 100);
  const quantumSafePct = Math.max(0, 100 - quantumVulnPct);

  // Algorithm breakdown for the Cryptographic Matrix
  const algoDistribution = useMemo(() => {
    const map: Record<string, { count: number; category: string; isVuln: boolean; maxRisk: string }> = {};
    findings.forEach((f, idx) => {
      const risk = riskAssessments[idx];
      const isVuln = risk?.quantumRisk === 'HIGH' || risk?.quantumRiskResult?.quantumVulnerable;
      const riskLevel = risk?.riskLevel || 'LOW';
      if (!map[f.algorithm]) {
        map[f.algorithm] = {
          count: 0,
          category: f.assetCategory || f.purpose || 'CIPHER',
          isVuln: !!isVuln,
          maxRisk: riskLevel,
        };
      }
      map[f.algorithm].count += 1;
    });
    return Object.entries(map).sort((a, b) => b[1].count - a[1].count);
  }, [findings, riskAssessments]);

  // Classify algorithms for Shor's vs Grover's impact
  const shorAlgorithms = useMemo(() => {
    const asymmetricAlgos = ['RSA', 'ECDSA', 'ECDH', 'DSA', 'Diffie-Hellman', 'DH', 'Elliptic Curve'];
    return findings.filter((f, idx) => {
      const risk = riskAssessments[idx];
      const isVuln = risk?.quantumRisk === 'HIGH' || risk?.quantumRiskResult?.quantumVulnerable;
      const isAsymmetric = asymmetricAlgos.some(algo => 
        f.algorithm.toUpperCase().includes(algo.toUpperCase())
      );
      return isAsymmetric && isVuln;
    });
  }, [findings, riskAssessments]);

  const groverAlgorithms = useMemo(() => {
    const symmetricHashAlgos = ['AES', 'SHA', 'MD5', 'HMAC', 'HASH'];
    return findings.filter((f) => {
      const isSymmetric = symmetricHashAlgos.some(algo => 
        f.algorithm.toUpperCase().includes(algo.toUpperCase())
      );
      return isSymmetric;
    });
  }, [findings]);

  // PQC recommendations summary
  const pqcTargets = useMemo(() => {
    const targets: Record<string, number> = {};
    pqcRecommendations.forEach((pqc) => {
      if (pqc.recommendedAlgorithm) {
        targets[pqc.recommendedAlgorithm] = (targets[pqc.recommendedAlgorithm] || 0) + 1;
      }
    });
    return Object.entries(targets).sort((a, b) => b[1] - a[1]);
  }, [pqcRecommendations]);

  // Prioritize findings for Recent Findings table (Critical/High and Quantum Vulnerable first)
  const prioritizedFindings = findings
    .map((finding, idx) => ({
      finding,
      risk: riskAssessments[idx],
      pqc: pqcRecommendations[idx],
      index: idx,
    }))
    .sort((a, b) => {
      const riskWeight: Record<string, number> = { CRITICAL: 4, HIGH: 3, MEDIUM: 2, LOW: 1 };
      const aWeight = (riskWeight[a.risk?.riskLevel || 'LOW'] || 0) + (a.risk?.quantumRisk === 'HIGH' ? 2 : 0);
      const bWeight = (riskWeight[b.risk?.riskLevel || 'LOW'] || 0) + (b.risk?.quantumRisk === 'HIGH' ? 2 : 0);
      return bWeight - aWeight;
    })
    .slice(0, 6);

  return (
    <div className="ecdat-dashboard-container">
      {/* 1. Page Header & Context */}
      <div className="view-header">
        <div className="view-title-group">
          <div className="title-with-badge">
            <h1 className="view-title">Cryptographic Security Overview</h1>
            <span className="platform-tag">Enterprise SecOps</span>
          </div>
          <p className="view-subtitle">
            Visibility into cryptographic assets, quantum exposure, and migration readiness.
          </p>
          {/* Contextual Metadata Row - Only show fields that exist */}
          {(analysisData.inputName || analysisData.inputType || analysisData.context?.applicationName) && (
            <div className="view-metadata-row">
              {analysisData.inputName && (
                <div className="metadata-item">
                  <span className="metadata-label">Target:</span>
                  <span className="metadata-value font-mono" title={analysisData.inputName}>
                    {analysisData.inputName.length > 40 ? analysisData.inputName.substring(0, 40) + '...' : analysisData.inputName}
                  </span>
                </div>
              )}
              {analysisData.inputType && (
                <div className="metadata-item">
                  <span className="metadata-label">Source:</span>
                  <span className="metadata-value">{analysisData.inputType}</span>
                </div>
              )}
              {analysisData.context?.applicationName && (
                <div className="metadata-item">
                  <span className="metadata-label">Application:</span>
                  <span className="metadata-value">{analysisData.context.applicationName}</span>
                </div>
              )}
            </div>
          )}
        </div>
        <div className="view-actions">
          <button className="btn-secondary" onClick={() => onNavigate('scan')} title="Run a new project scan">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
              <polyline points="17 8 12 3 7 8" />
              <line x1="12" y1="3" x2="12" y2="9" />
            </svg>
            New Scan
          </button>
          <button className="btn-primary" onClick={() => onNavigate('cbom')} title="View Cryptography Bill of Materials">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
              <polyline points="14 2 14 8 20 8" />
              <line x1="16" y1="13" x2="8" y2="13" />
              <line x1="16" y1="17" x2="8" y2="17" />
              <polyline points="10 9 9 9 8 9" />
            </svg>
            View CBOM
          </button>
        </div>
      </div>

      {/* 2. Top Summary KPI Cards (5 Compact Cards matching Image 1) */}
      <div className="dashboard-kpi-grid">
        {/* Card 1: Total Crypto Assets */}
        <div
          className="kpi-card"
          onClick={() => onNavigate('inventory')}
          role="button"
          tabIndex={0}
          title="Click to view all discovered cryptographic assets in inventory"
        >
          <div className="kpi-card-header">
            <span className="kpi-card-label">Total Crypto Assets</span>
            <div className="kpi-icon-pill primary">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="2" y="2" width="20" height="8" rx="2" ry="2" />
                <rect x="2" y="14" width="20" height="8" rx="2" ry="2" />
                <line x1="6" y1="6" x2="6.01" y2="6" />
                <line x1="6" y1="18" x2="6.01" y2="18" />
              </svg>
            </div>
          </div>
          <div className="kpi-card-body">
            <span className="kpi-card-val">{totalAssets}</span>
            <span className="kpi-delta-tag positive">
              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <polyline points="18 15 12 9 6 15" />
              </svg>
              Active
            </span>
          </div>
          <div className="kpi-card-footer">
            <span>Discovered cryptographic primitives</span>
          </div>
        </div>

        {/* Card 2: Quantum Vulnerable */}
        <div
          className="kpi-card"
          onClick={() => onNavigate('quantum')}
          role="button"
          tabIndex={0}
          title="Click to view Quantum Risk exposure analysis"
        >
          <div className="kpi-card-header">
            <span className="kpi-card-label">Quantum Vulnerable</span>
            <div className="kpi-icon-pill critical">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="2" y1="12" x2="22" y2="12" />
                <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
              </svg>
            </div>
          </div>
          <div className="kpi-card-body">
            <span className="kpi-card-val text-critical">{quantumVulnCount}</span>
            <span className="kpi-delta-tag negative">
              <svg width="10" height="10" viewBox="0 0 24 24" fill="currentColor">
                <circle cx="12" cy="12" r="6" />
              </svg>
              Shor Risk
            </span>
          </div>
          <div className="kpi-card-footer">
            <span>Vulnerable to Shor&apos;s algorithm (SNDL)</span>
          </div>
        </div>

        {/* Card 3: High / Critical Risk */}
        <div
          className="kpi-card"
          onClick={() => onNavigate('inventory')}
          role="button"
          tabIndex={0}
          title="Click to filter high and critical risk assets"
        >
          <div className="kpi-card-header">
            <span className="kpi-card-label">High / Critical Risk</span>
            <div className="kpi-icon-pill alert">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
                <line x1="12" y1="9" x2="12" y2="13" />
                <line x1="12" y1="17" x2="12.01" y2="17" />
              </svg>
            </div>
          </div>
          <div className="kpi-card-body">
            <span className="kpi-card-val text-high">{highCriticalCount}</span>
            <span className="kpi-delta-tag alert">
              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <polyline points="18 15 12 9 6 15" />
              </svg>
              Elevated
            </span>
          </div>
          <div className="kpi-card-footer">
            <span>{criticalCount} Critical · {highCount} High classical risk</span>
          </div>
        </div>

        {/* Card 4: Migration Required */}
        <div
          className="kpi-card"
          onClick={() => onNavigate('pqc')}
          role="button"
          tabIndex={0}
          title="Click to view Post-Quantum Cryptography Migration plans"
        >
          <div className="kpi-card-header">
            <span className="kpi-card-label">Migration Required</span>
            <div className="kpi-icon-pill warning">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="16 3 21 3 21 8" />
                <line x1="4" y1="20" x2="21" y2="3" />
                <polyline points="21 16 21 21 16 21" />
                <line x1="15" y1="15" x2="21" y2="21" />
              </svg>
            </div>
          </div>
          <div className="kpi-card-body">
            <span className="kpi-card-val text-warning">{migrationRequiredCount}</span>
            <span className="kpi-delta-tag neutral">
              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="16 3 21 3 21 8" />
                <line x1="4" y1="20" x2="21" y2="3" />
              </svg>
              Mosca &gt; 0
            </span>
          </div>
          <div className="kpi-card-footer">
            <span>Exceeds Mosca threat horizon</span>
          </div>
        </div>

        {/* Card 5: PQC Recommendations */}
        <div
          className="kpi-card"
          onClick={() => onNavigate('pqc')}
          role="button"
          tabIndex={0}
          title="Click to explore NIST standard recommendations (ML-KEM, ML-DSA, SLH-DSA)"
        >
          <div className="kpi-card-header">
            <span className="kpi-card-label">PQC Recommendations</span>
            <div className="kpi-icon-pill indigo">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                <path d="m9 12 2 2 4-4" />
              </svg>
            </div>
          </div>
          <div className="kpi-card-body">
            <span className="kpi-card-val text-primary">{pqcRecCount}</span>
            <span className="kpi-delta-tag positive">
              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <polyline points="20 6 9 17 4 12" />
              </svg>
              NIST Ready
            </span>
          </div>
          <div className="kpi-card-footer">
            <span>FIPS 203, 204, 205 standardized</span>
          </div>
        </div>
      </div>

      {/* 3. Main Analytics Area (2-Column Balanced Grid matching Image 1) */}
      <div className="dashboard-charts-grid">
        {/* Left: Risk Distribution & Cryptographic Matrix */}
        <div className="chart-card">
          <div className="chart-card-header">
            <div>
              <h3 className="chart-card-title">Risk Distribution</h3>
              <span className="chart-card-sub">Classical severity rating across discovered primitives</span>
            </div>
            <button className="card-link-btn" onClick={() => onNavigate('inventory')}>
              Details →
            </button>
          </div>

          <div className="donut-chart-container">
            <div className="donut-svg-wrapper">
              <svg viewBox="0 0 100 100" className="donut-svg">
                <circle cx="50" cy="50" r={radius} className="donut-bg" />
                {critDash > 0 && (
                  <circle
                    cx="50"
                    cy="50"
                    r={radius}
                    className={`donut-segment crit ${hoveredDonutSegment === 'critical' ? 'hovered' : ''}`}
                    strokeDasharray={`${critDash} ${circumference - critDash}`}
                    strokeDashoffset={critOffset}
                    onMouseEnter={() => setHoveredDonutSegment('critical')}
                    onMouseLeave={() => setHoveredDonutSegment(null)}
                  />
                )}
                {highDash > 0 && (
                  <circle
                    cx="50"
                    cy="50"
                    r={radius}
                    className={`donut-segment high ${hoveredDonutSegment === 'high' ? 'hovered' : ''}`}
                    strokeDasharray={`${highDash} ${circumference - highDash}`}
                    strokeDashoffset={highOffset}
                    onMouseEnter={() => setHoveredDonutSegment('high')}
                    onMouseLeave={() => setHoveredDonutSegment(null)}
                  />
                )}
                {medDash > 0 && (
                  <circle
                    cx="50"
                    cy="50"
                    r={radius}
                    className={`donut-segment med ${hoveredDonutSegment === 'medium' ? 'hovered' : ''}`}
                    strokeDasharray={`${medDash} ${circumference - medDash}`}
                    strokeDashoffset={medOffset}
                    onMouseEnter={() => setHoveredDonutSegment('medium')}
                    onMouseLeave={() => setHoveredDonutSegment(null)}
                  />
                )}
                {lowDash > 0 && (
                  <circle
                    cx="50"
                    cy="50"
                    r={radius}
                    className={`donut-segment low ${hoveredDonutSegment === 'low' ? 'hovered' : ''}`}
                    strokeDasharray={`${lowDash} ${circumference - lowDash}`}
                    strokeDashoffset={lowOffset}
                    onMouseEnter={() => setHoveredDonutSegment('low')}
                    onMouseLeave={() => setHoveredDonutSegment(null)}
                  />
                )}
              </svg>
              <div className="donut-center-text">
                <span className="donut-center-val">{totalAssets}</span>
                <span className="donut-center-lbl">Total Assets</span>
              </div>
            </div>

            <div className="donut-legend-list">
              <div
                className={`donut-legend-item ${hoveredDonutSegment === 'critical' ? 'active' : ''}`}
                onMouseEnter={() => setHoveredDonutSegment('critical')}
                onMouseLeave={() => setHoveredDonutSegment(null)}
              >
                <span className="legend-dot critical"></span>
                <span className="legend-name">Critical</span>
                <span className="legend-count">{criticalCount}</span>
                <span className="legend-pct">({critPct}%)</span>
              </div>
              <div
                className={`donut-legend-item ${hoveredDonutSegment === 'high' ? 'active' : ''}`}
                onMouseEnter={() => setHoveredDonutSegment('high')}
                onMouseLeave={() => setHoveredDonutSegment(null)}
              >
                <span className="legend-dot high"></span>
                <span className="legend-name">High</span>
                <span className="legend-count">{highCount}</span>
                <span className="legend-pct">({highPct}%)</span>
              </div>
              <div
                className={`donut-legend-item ${hoveredDonutSegment === 'medium' ? 'active' : ''}`}
                onMouseEnter={() => setHoveredDonutSegment('medium')}
                onMouseLeave={() => setHoveredDonutSegment(null)}
              >
                <span className="legend-dot medium"></span>
                <span className="legend-name">Medium</span>
                <span className="legend-count">{mediumCount}</span>
                <span className="legend-pct">({medPct}%)</span>
              </div>
              <div
                className={`donut-legend-item ${hoveredDonutSegment === 'low' ? 'active' : ''}`}
                onMouseEnter={() => setHoveredDonutSegment('low')}
                onMouseLeave={() => setHoveredDonutSegment(null)}
              >
                <span className="legend-dot low"></span>
                <span className="legend-name">Low</span>
                <span className="legend-count">{lowCount}</span>
                <span className="legend-pct">({lowPct}%)</span>
              </div>
            </div>
          </div>

          {/* Cryptographic Algorithm Distribution Matrix */}
          {algoDistribution.length > 0 && (
            <div className="matrix-breakdown-wrap">
              <div className="matrix-breakdown-title">Cryptographic Matrix Breakdown</div>
              <div className="matrix-chips-grid">
                {algoDistribution.slice(0, 6).map(([algo, info]) => (
                  <div key={algo} className={`matrix-chip ${info.isVuln ? 'vuln' : 'safe'}`} onClick={() => onNavigate('inventory')}>
                    <span className="matrix-chip-algo font-mono font-bold">{algo}</span>
                    <span className="matrix-chip-count font-mono">{info.count}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* Right: Quantum Exposure & Threat Spectrum */}
        <div className="chart-card">
          <div className="chart-card-header">
            <div>
              <h3 className="chart-card-title">Quantum Exposure & Threat Spectrum</h3>
              <span className="chart-card-sub">Post-quantum readiness assessment by algorithm category</span>
            </div>
            <button className="card-link-btn" onClick={() => onNavigate('quantum')}>
              Assessment →
            </button>
          </div>

          <div className="quantum-exposure-content">
            {/* Segmented Exposure Bar */}
            <div className="exposure-bar-wrap">
              <div className="exposure-bar-labels">
                <span className="font-semibold text-critical">
                  {quantumVulnCount} Vulnerable ({quantumVulnPct}%)
                </span>
                <span className="font-semibold text-low">
                  {quantumSafeCount} Safe ({quantumSafePct}%)
                </span>
              </div>
              <div className="exposure-bar-track">
                <div
                  className="exposure-bar-fill vuln"
                  style={{ width: `${quantumVulnPct}%` }}
                  title={`${quantumVulnCount} Quantum Vulnerable Assets (${quantumVulnPct}%)`}
                ></div>
                <div
                  className="exposure-bar-fill safe"
                  style={{ width: `${quantumSafePct}%` }}
                  title={`${quantumSafeCount} Quantum Safe Assets (${quantumSafePct}%)`}
                ></div>
              </div>
            </div>

            {/* Shor's Algorithm Impact */}
            <div className="quantum-section">
              <div className="quantum-section-header">
                <span className="quantum-section-title">Shor&apos;s Algorithm Impact</span>
                <span className="quantum-section-badge critical">Asymmetric Vulnerable</span>
              </div>
              <div className="quantum-section-desc">
                Public-key cryptography vulnerable to quantum factoring (RSA, ECC, DSA, Diffie-Hellman)
              </div>
              {shorAlgorithms.length > 0 ? (
                <div className="quantum-algo-list">
                  {shorAlgorithms.slice(0, 5).map((f, idx) => (
                    <div key={idx} className="quantum-algo-item vuln">
                      <span className="quantum-algo-name font-mono">{f.algorithm}</span>
                      <span className="quantum-algo-count">1</span>
                    </div>
                  ))}
                  {shorAlgorithms.length > 5 && (
                    <div className="quantum-algo-more">
                      +{shorAlgorithms.length - 5} more
                    </div>
                  )}
                </div>
              ) : (
                <div className="quantum-empty-state">No asymmetric quantum-vulnerable algorithms detected</div>
              )}
            </div>

            {/* Grover's Algorithm Impact */}
            <div className="quantum-section">
              <div className="quantum-section-header">
                <span className="quantum-section-title">Grover&apos;s Algorithm Impact</span>
                <span className="quantum-section-badge warning">Degraded Security</span>
              </div>
              <div className="quantum-section-desc">
                Symmetric cryptography and hash functions with quadratic speedup (AES, SHA, HMAC)
              </div>
              {groverAlgorithms.length > 0 ? (
                <div className="quantum-algo-list">
                  {groverAlgorithms.slice(0, 5).map((f, idx) => (
                    <div key={idx} className="quantum-algo-item warning">
                      <span className="quantum-algo-name font-mono">{f.algorithm}</span>
                      <span className="quantum-algo-count">1</span>
                    </div>
                  ))}
                  {groverAlgorithms.length > 5 && (
                    <div className="quantum-algo-more">
                      +{groverAlgorithms.length - 5} more
                    </div>
                  )}
                </div>
              ) : (
                <div className="quantum-empty-state">No symmetric/hash algorithms detected</div>
              )}
            </div>

            {/* Recommended PQC Targets */}
            {pqcTargets.length > 0 && (
              <div className="quantum-section">
                <div className="quantum-section-header">
                  <span className="quantum-section-title">Recommended PQC Targets</span>
                  <span className="quantum-section-badge indigo">NIST Standards</span>
                </div>
                <div className="quantum-pqc-list">
                  {pqcTargets.slice(0, 4).map(([algo, count], idx) => (
                    <div key={idx} className="quantum-pqc-item">
                      <span className="quantum-pqc-name font-mono">{algo}</span>
                      <span className="quantum-pqc-count">{count} recommendations</span>
                    </div>
                  ))}
                  {pqcTargets.length > 4 && (
                    <div className="quantum-pqc-more">
                      +{pqcTargets.length - 4} more algorithms
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 4. Bottom Grid (2-Column Balanced: Ranked Mosca on Left, Findings Table on Right) */}
      <div className="dashboard-bottom-grid">
        {/* Left: Dedicated Numbered Ranked Mosca Assessment Section */}
        <div className="mosca-assessment-card">
          <div className="mosca-card-header">
            <div className="mosca-header-left">
              <div className="mosca-icon-badge">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="6" x2="12" y2="12" />
                  <line x1="12" y1="12" x2="16" y2="14" />
                </svg>
              </div>
              <div>
                <h3 className="mosca-title">Quantum Exposure Assessment</h3>
                <p className="mosca-subtitle">Mosca&apos;s Theorem Exposure Horizon</p>
              </div>
            </div>
            <div className="mosca-header-right">
              <button className="btn-link" onClick={() => onNavigate('quantum')}>
                Details →
              </button>
            </div>
          </div>

          <div className="mosca-assessment-body">
            {/* Ranked Numbered Capsules (Image 1 Style) */}
            <div className="ranked-exposure-grid">
              <div className="ranked-capsule">
                <div className="ranked-num">1</div>
                <div className="ranked-info">
                  <span className="ranked-label">Migration Time (X)</span>
                  <span className="ranked-val font-mono">{migrationTime} yrs</span>
                  <span className="ranked-desc">Time required to migrate organizational systems to PQC</span>
                </div>
              </div>

              <div className="ranked-capsule">
                <div className="ranked-num">2</div>
                <div className="ranked-info">
                  <span className="ranked-label">Data Lifetime (Y)</span>
                  <span className="ranked-val font-mono">{dataLifetime} yrs</span>
                  <span className="ranked-desc">Time the encrypted data must remain confidential</span>
                </div>
              </div>

              <div className="ranked-capsule">
                <div className="ranked-num">3</div>
                <div className="ranked-info">
                  <span className="ranked-label">Threat Horizon (Z)</span>
                  <span className="ranked-val font-mono">{threatHorizon} yrs</span>
                  <span className="ranked-desc">Estimated time until a Cryptanalytically Relevant Quantum Computer</span>
                </div>
              </div>

              <div className={`ranked-capsule highlight ${moscaConditionMet ? 'threat-active' : 'threat-safe'}`}>
                <div className="ranked-num">4</div>
                <div className="ranked-info">
                  <span className="ranked-label">Total Exposure (X + Y &gt; Z)</span>
                  <span className="ranked-val font-mono font-bold text-critical">
                    {totalExposure} yrs vs {threatHorizon} yrs
                  </span>
                  <span className="ranked-desc">
                    {moscaConditionMet
                      ? 'Condition met: Systems vulnerable to Store Now, Decrypt Later (SNDL).'
                      : 'Condition safe: Threat horizon exceeds required operational security window.'}
                  </span>
                </div>
              </div>
            </div>

            {/* Mosca Verdict Strip */}
            <div className={`mosca-verdict-strip ${moscaConditionMet ? 'critical' : 'safe'}`}>
              <div className="verdict-strip-left">
                <span className="font-mono font-bold">
                  {moscaConditionMet ? 'STATUS: MIGRATION REQUIRED' : 'STATUS: QUANTUM SAFE'}
                </span>
                <span className="verdict-detail">
                  {moscaConditionMet
                    ? `${totalExposure} yrs total exposure exceeds ${threatHorizon} yrs threat horizon.`
                    : `${totalExposure} yrs total exposure is within ${threatHorizon} yrs threat horizon.`}
                </span>
              </div>
              <button className="btn-secondary btn-sm" onClick={() => onNavigate('pqc')}>
                PQC Roadmap →
              </button>
            </div>
          </div>
        </div>

        {/* Right: Recent Cryptographic Findings Table */}
        <div className="card-panel findings-panel-card">
          <div className="card-panel-header">
            <div>
              <h3 className="card-panel-title">Recent Cryptographic Findings</h3>
              <span className="card-panel-sub">Highest priority cryptographic primitives requiring analyst review</span>
            </div>
            <button className="btn-link" onClick={() => onNavigate('inventory')}>
              View Full Inventory →
            </button>
          </div>

          <div className="table-container">
            <table className="enterprise-table">
              <thead>
                <tr>
                  <th style={{ width: '22%' }}>Algorithm</th>
                  <th style={{ width: '20%' }}>Purpose</th>
                  <th style={{ width: '12%' }}>Risk</th>
                  <th style={{ width: '18%' }}>Quantum Status</th>
                  <th style={{ width: '18%' }}>Source</th>
                  <th style={{ width: '10%', textAlign: 'right' }}>Action</th>
                </tr>
              </thead>
              <tbody>
                {prioritizedFindings.length === 0 ? (
                  <tr>
                    <td colSpan={6} style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                      No cryptographic findings detected in current scan target.
                    </td>
                  </tr>
                ) : (
                  prioritizedFindings.map(({ finding, risk, index }) => {
                    const riskLevel = risk?.riskLevel || 'LOW';
                    const isQuantumVuln = risk?.quantumRisk === 'HIGH' || risk?.quantumRiskResult?.quantumVulnerable;
                    const fileName = finding.file.split(/[\\/]/).pop() || finding.file;
                    const purposeDisplay = formatTitleCase(finding.purpose);

                    return (
                      <tr
                        key={index}
                        className="clickable-row"
                        onClick={() => onSelectFinding(index)}
                        title={`Inspect details for ${finding.algorithm} at ${fileName}:${finding.line}`}
                      >
                        <td>
                          <div className="algo-cell-block">
                            <span className="algo-primary-title" title={finding.algorithm}>{finding.algorithm}</span>
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
                          <span className="table-purpose-text" title={purposeDisplay}>{purposeDisplay}</span>
                        </td>
                        <td>
                          <span className={`risk-badge ${riskLevel.toLowerCase()}`}>
                            {riskLevel}
                          </span>
                        </td>
                        <td>
                          <span className={`quantum-badge ${isQuantumVuln ? 'vulnerable' : 'safe'}`}>
                            <span className={`status-dot-sm ${isQuantumVuln ? 'vuln' : 'safe'}`}></span>
                            {isQuantumVuln ? 'Vulnerable' : 'Safe'}
                          </span>
                        </td>
                        <td>
                          <div className="source-location-cell font-mono" title={finding.file}>
                            <span className="source-file">{fileName}</span>
                            <span className="source-line">:{finding.line}</span>
                          </div>
                        </td>
                        <td style={{ textAlign: 'right' }}>
                          <button
                            className="btn-secondary btn-sm"
                            onClick={(e) => {
                              e.stopPropagation();
                              onSelectFinding(index);
                            }}
                          >
                            View
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

      <div className="dashboard-quick-actions">
        <div className="quick-actions-header">
          <h4 className="quick-actions-title">Security Analyst Quick Actions</h4>
        </div>
        <div className="quick-actions-grid">
          <button className="quick-action-card" onClick={() => onNavigate('scan')}>
            <div className="qa-icon primary">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="17 8 12 3 7 8" />
                <line x1="12" y1="3" x2="12" y2="9" />
              </svg>
            </div>
            <div className="qa-text">
              <span className="qa-title">+ New Scan</span>
              <span className="qa-sub">Upload ZIP or target path</span>
            </div>
          </button>

          <button className="quick-action-card" onClick={() => onNavigate('inventory')}>
            <div className="qa-icon indigo">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="2" y="2" width="20" height="8" rx="2" ry="2" />
                <rect x="2" y="14" width="20" height="8" rx="2" ry="2" />
                <line x1="6" y1="6" x2="6.01" y2="6" />
                <line x1="6" y1="18" x2="6.01" y2="18" />
              </svg>
            </div>
            <div className="qa-text">
              <span className="qa-title">View Inventory</span>
              <span className="qa-sub">Full cryptographic assets</span>
            </div>
          </button>

          <button className="quick-action-card" onClick={() => onNavigate('quantum')}>
            <div className="qa-icon critical">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="2" y1="12" x2="22" y2="12" />
                <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
              </svg>
            </div>
            <div className="qa-text">
              <span className="qa-title">View Quantum Risk</span>
              <span className="qa-sub">Mosca &amp; Shor modeling</span>
            </div>
          </button>

          <button className="quick-action-card" onClick={() => onNavigate('pqc')}>
            <div className="qa-icon warning">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <polyline points="16 3 21 3 21 8" />
                <line x1="4" y1="20" x2="21" y2="3" />
                <polyline points="21 16 21 21 16 21" />
                <line x1="15" y1="15" x2="21" y2="21" />
              </svg>
            </div>
            <div className="qa-text">
              <span className="qa-title">View PQC Migration</span>
              <span className="qa-sub">NIST standardization</span>
            </div>
          </button>

          <button className="quick-action-card" onClick={() => onNavigate('cbom')}>
            <div className="qa-icon success">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
              </svg>
            </div>
            <div className="qa-text">
              <span className="qa-title">View CBOM</span>
              <span className="qa-sub">CycloneDX export &amp; spec</span>
            </div>
          </button>
        </div>
      </div>
    </div>
  );
};
